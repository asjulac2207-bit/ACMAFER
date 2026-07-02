package com.acmafer.modulos.usuarios.servicio;


import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.servicio.EmailService;
import com.acmafer.modulos.usuarios.entidad.TokenRecuperacion;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.TokenRecuperacionRepository;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final UsuarioRepository usuarioRepo;
    private final TokenRecuperacionRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    @Value("${acmafer.token.expiracion-minutos:30}")
    private int expiracionMinutos;

    @Transactional
    public void solicitarRecuperacion(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new BusinessException("No existe cuenta con ese correo"));
        tokenRepo.deleteByUsuarioId(usuario.getId());
        String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
        TokenRecuperacion tr = TokenRecuperacion.builder()
            .usuario(usuario)
            .token(codigo)
            .fechaExpiracion(LocalDateTime.now().plusMinutes(expiracionMinutos))
            .build();
        tokenRepo.save(tr);
        emailService.enviarTokenRecuperacion(usuario, codigo);
        log.info("Token recuperación generado para {}", email);
    }

    @Transactional
    public void cambiarConCodigo(String token, String nuevaClave) {
        TokenRecuperacion tr = tokenRepo.findByToken(token)
            .orElseThrow(() -> new BusinessException("Código inválido o expirado"));
        if (!tr.estaVigente()) {
            throw new BusinessException("El código expiró. Solicita uno nuevo.");
        }
        Usuario u = tr.getUsuario();
        u.setClave(encoder.encode(nuevaClave));
        u.setIntentosFallidos(0);
        u.setBloqueado(false);
        usuarioRepo.save(u);
        tr.setUsado(true);
        tokenRepo.save(tr);
        log.info("Contraseña restablecida para {}", u.getEmail());
    }

    @Transactional
    public void cambiarContrasena(Usuario usuario, String claveActual, String nuevaClave) {
        if (!encoder.matches(claveActual, usuario.getClave())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }
        usuario.setClave(encoder.encode(nuevaClave));
        usuarioRepo.save(usuario);
    }
}
