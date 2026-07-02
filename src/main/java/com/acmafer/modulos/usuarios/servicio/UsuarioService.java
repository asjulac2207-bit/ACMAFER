package com.acmafer.modulos.usuarios.servicio;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.servicio.EmailService;
import com.acmafer.modulos.notificaciones.repositorio.NotificacionRepository;
import com.acmafer.modulos.tareas.repositorio.AsignacionTareaRepository;
import com.acmafer.modulos.usuarios.dto.UsuarioRegistroDTO;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final AsignacionTareaRepository asignacionTareaRepo;
    private final NotificacionRepository notificacionRepo;

    @Value("${acmafer.captcha.intentos-antes-de-activar:3}")
    private int maxIntentos;

    @Transactional
    public Usuario registrar(UsuarioRegistroDTO dto) {
        if (usuarioRepo.existsByEmail(dto.getEmail()))
            throw new BusinessException("El correo ya está registrado");
        if (usuarioRepo.existsByDocumento(dto.getDocumento()))
            throw new BusinessException("El documento ya está registrado");

        Usuario u = Usuario.builder()
                .documento(dto.getDocumento())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .celular(dto.getCelular())
                .email(dto.getEmail())
                .clave(encoder.encode(dto.getClave()))
                .rol(Rol.CLIENTE)
                .estado("Activo")
                .build();
        Usuario guardado = usuarioRepo.save(u);
        emailService.enviarBienvenida(guardado);
        log.info("Usuario registrado: {} rol=CLIENTE", guardado.getEmail());
        return guardado;
    }

    @Transactional
    public Usuario registrarPorAdmin(UsuarioRegistroDTO dto, Rol rol) {
        if (usuarioRepo.existsByEmail(dto.getEmail()))
            throw new BusinessException("El correo ya está registrado");
        if (usuarioRepo.existsByDocumento(dto.getDocumento()))
            throw new BusinessException("El documento ya está registrado");

        Usuario u = Usuario.builder()
                .documento(dto.getDocumento())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .celular(dto.getCelular())
                .email(dto.getEmail())
                .clave(encoder.encode(dto.getClave()))
                .rol(rol)
                .estado("Activo")
                .build();
        Usuario guardado = usuarioRepo.save(u);
        emailService.enviarBienvenida(guardado);
        log.info("Admin registró usuario: {} rol={}", guardado.getEmail(), rol);
        return guardado;
    }

    @Transactional
    public void registrarIntentoFallido(String email) {
        usuarioRepo.findByEmail(email).ifPresent(u -> {
            int intentos = u.getIntentosFallidos() + 1;
            u.setIntentosFallidos(intentos);
            if (intentos >= 5) {
                u.setBloqueado(true);
                u.setFechaBloqueo(LocalDateTime.now());
                log.warn("Cuenta bloqueada: {}", email);
                emailService.notificarCuentaBloqueada(u);
            }
            usuarioRepo.save(u);
        });
    }

    @Transactional
    public void resetearIntentosFallidos(String email) {
        usuarioRepo.findByEmail(email).ifPresent(u -> {
            u.setIntentosFallidos(0);
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepo.save(u);
        });
    }

    @Transactional
    public Usuario actualizar(Long id, UsuarioRegistroDTO dto, Rol nuevoRol) {
        Usuario u = buscarPorId(id);
        Rol rolAnterior = u.getRol();

        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCelular(dto.getCelular());
        u.setEmail(dto.getEmail());
        u.setRol(nuevoRol);
        if (dto.getClave() != null && !dto.getClave().isBlank())
            u.setClave(encoder.encode(dto.getClave()));

        Usuario guardado = usuarioRepo.save(u);

        // Si el usuario deja de ser TRABAJADOR, ya no debe conservar tareas
        // asignadas ni las notificaciones asociadas a ellas (evita que un
        // ADMIN/SUPERVISOR/VENDEDOR/CLIENTE siga viendo "Mis Tareas" viejas
        // o reciba notificaciones huérfanas tras el cambio de rol).
        if (rolAnterior == Rol.TRABAJADOR && nuevoRol != Rol.TRABAJADOR) {
            limpiarTareasAsignadas(guardado.getId());
            log.info("Rol de {} cambió de TRABAJADOR a {}: se limpiaron sus tareas/notificaciones asignadas",
                    guardado.getEmail(), nuevoRol);
        }

        return guardado;
    }

    @Transactional
    public void limpiarTareasAsignadas(Long idUsuario) {
        notificacionRepo.deleteByDestinatarioIdAndTipo(idUsuario, "TAREA");
        asignacionTareaRepo.deleteByEmpleadoId(idUsuario);
    }

    @Transactional
    public void cambiarEstado(Long id, String estado) {
        Usuario u = buscarPorId(id);
        u.setEstado(estado);
        if ("Activo".equals(estado)) {
            u.setBloqueado(false);
            u.setIntentosFallidos(0);
        }
        usuarioRepo.save(u);
    }

    public Page<Usuario> listar(String busqueda, Pageable pageable) {
        if (busqueda != null && !busqueda.isBlank())
            return usuarioRepo.buscar(busqueda, pageable);
        return usuarioRepo.findAll(pageable);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + id));
    }

    public List<Usuario> listarTrabajadoresActivos() {
        return usuarioRepo.findActivosByRol(Rol.TRABAJADOR);
    }

    public boolean requiereCaptcha(String email) {
        return usuarioRepo.findByEmail(email)
                .map(u -> u.getIntentosFallidos() >= maxIntentos)
                .orElse(false);
    }

    @Transactional
    public Usuario buscarOCrearPorGoogle(String email, String nombreCompleto, String fotoUrl) {
        return usuarioRepo.findByEmail(email).map(u -> {
            // Usuario ya existe: actualizar foto si cambió
            if (fotoUrl != null && !fotoUrl.equals(u.getFotoUrl())) {
                u.setFotoUrl(fotoUrl);
                usuarioRepo.save(u);
            }
            return u;
        }).orElseGet(() -> {
            String nombre = "Usuario";
            String apellido = "Google";
            if (nombreCompleto != null && !nombreCompleto.isBlank()) {
                String[] partes = nombreCompleto.trim().split(" ", 2);
                nombre = partes[0];
                apellido = partes.length > 1 ? partes[1] : "Google";
            }

            Usuario u = Usuario.builder()
                    .documento("GOOGLE-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .nombre(nombre)
                    .apellido(apellido)
                    .email(email)
                    .clave(encoder.encode(java.util.UUID.randomUUID().toString()))
                    .rol(Rol.CLIENTE)
                    .estado("Activo")
                    .proveedor("GOOGLE")
                    .fotoUrl(fotoUrl)
                    .build();
            Usuario guardado = usuarioRepo.save(u);
            log.info("Usuario creado vía Google: {} rol=CLIENTE", guardado.getEmail());
            return guardado;
        });
    }
}