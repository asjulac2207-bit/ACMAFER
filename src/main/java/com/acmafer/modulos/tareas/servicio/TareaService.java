package com.acmafer.modulos.tareas.servicio;


import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.servicio.EmailService;
import com.acmafer.modulos.notificaciones.entidad.Notificacion;
import com.acmafer.modulos.notificaciones.repositorio.NotificacionRepository;
import com.acmafer.modulos.tareas.dto.TareaDTO;
import com.acmafer.modulos.tareas.entidad.AsignacionTarea;
import com.acmafer.modulos.tareas.entidad.ComentarioTarea;
import com.acmafer.modulos.tareas.entidad.Tarea;
import com.acmafer.modulos.tareas.repositorio.AsignacionTareaRepository;
import com.acmafer.modulos.tareas.repositorio.ComentarioTareaRepository;
import com.acmafer.modulos.tareas.repositorio.TareaRepository;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TareaService {

    private final TareaRepository tareaRepo;
    private final AsignacionTareaRepository asignacionRepo;
    private final ComentarioTareaRepository comentarioRepo;
    private final NotificacionRepository notifRepo;
    private final UsuarioRepository usuarioRepo;
    private final EmailService emailService;

    @Transactional
    public Tarea crear(TareaDTO dto, Usuario creador) {
        Tarea tarea = Tarea.builder()
            .titulo(dto.getTitulo())
            .descripcion(dto.getDescripcion())
            .prioridad(dto.getPrioridad() != null ? dto.getPrioridad() : Tarea.Prioridad.MEDIA)
            .fechaVencimiento(dto.getFechaVencimiento())
            .build();
        Tarea guardada = tareaRepo.save(tarea);
        if (dto.getIdEmpleado() != null)
            asignar(guardada.getId(), dto.getIdEmpleado(), dto.getComentarioAdmin(), creador);
        log.info("Tarea creada: '{}' por {}", guardada.getTitulo(), creador.getEmail());
        return guardada;
    }

    @Transactional
    public AsignacionTarea asignar(Long idTarea, Long idEmpleado, String comentario, Usuario asignadoPor) {
        Tarea tarea = buscarPorId(idTarea);
        Usuario empleado = usuarioRepo.findById(idEmpleado)
            .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + idEmpleado));
        if (empleado.getRol() != Rol.TRABAJADOR)
            throw new BusinessException("Solo se puede asignar tareas a Trabajadores");

        AsignacionTarea asig = AsignacionTarea.builder()
            .tarea(tarea).empleado(empleado)
            .asignadoPor(asignadoPor).comentarioAdmin(comentario).build();
        AsignacionTarea guardada = asignacionRepo.save(asig);

        if (tarea.getEstado() == Tarea.Estado.NO_INICIADA) {
            tarea.setEstado(Tarea.Estado.EN_PROGRESO);
            tareaRepo.save(tarea);
        }
        notifRepo.save(Notificacion.builder()
            .destinatario(empleado)
            .mensaje("Nueva tarea asignada: " + tarea.getTitulo())
            .tipo("TAREA").urlDestino("/tareas/mis-tareas").build());
        emailService.notificarAsignacionTarea(empleado, tarea, asignadoPor);
        log.info("Tarea {} asignada a {} por {}", idTarea, empleado.getEmail(), asignadoPor.getEmail());
        return guardada;
    }

    @Transactional
    public Tarea cambiarEstado(Long idTarea, Tarea.Estado nuevoEstado, Usuario usuario) {
        Rol rol = usuario.getRol();
        boolean esAdminOSupervisor = rol == Rol.ADMINISTRADOR || rol == Rol.SUPERVISOR;
        boolean asignada = asignacionRepo.findByEmpleadoId(usuario.getId())
            .stream().anyMatch(a -> a.getTarea().getId().equals(idTarea));
        if (!esAdminOSupervisor && !asignada)
            throw new BusinessException("No tienes permiso para actualizar esta tarea");

        Tarea tarea = buscarPorId(idTarea);
        tarea.setEstado(nuevoEstado);
        Tarea guardada = tareaRepo.save(tarea);
        log.info("Tarea {} → {} por {}", idTarea, nuevoEstado, usuario.getEmail());

        // Si quien cambia el estado es el Trabajador asignado, se notifica
        // al administrador/supervisor que asignó la tarea.
        if (rol == Rol.TRABAJADOR) {
            String mensaje = usuario.getNombreCompleto() + " actualizó el estado de la tarea '"
                + guardada.getTitulo() + "' a: " + guardada.getEstado();
            notificarResponsablesTarea(guardada, mensaje, "ESTADO_TAREA");
        }
        return guardada;
    }

    @Transactional
    public ComentarioTarea agregarComentario(Long idTarea, String contenido, Usuario autor) {
        Tarea tarea = buscarPorId(idTarea);
        ComentarioTarea guardado = comentarioRepo.save(ComentarioTarea.builder()
            .tarea(tarea).autor(autor).contenido(contenido).build());

        // Si quien comenta es el Trabajador, se notifica al administrador/supervisor
        // que asignó la tarea, incluyendo el comentario y el estado actual.
        if (autor.getRol() == Rol.TRABAJADOR) {
            String resumen = contenido != null && contenido.length() > 100
                ? contenido.substring(0, 100) + "…" : contenido;
            String mensaje = autor.getNombreCompleto() + " comentó en la tarea '" + tarea.getTitulo()
                + "' (Estado: " + tarea.getEstado() + "): " + resumen;
            notificarResponsablesTarea(tarea, mensaje, "COMENTARIO_TAREA");
        }
        return guardado;
    }

    // ═══════════════════════════════════════════════════════════════
    // PRIVADO — Notifica a quien(es) asignaron la tarea (o, si no hay
    // dato de asignación, a todos los administradores activos) sobre
    // comentarios o cambios de estado hechos por el trabajador.
    // ═══════════════════════════════════════════════════════════════
    private void notificarResponsablesTarea(Tarea tarea, String mensaje, String tipo) {
        List<AsignacionTarea> asignaciones = asignacionRepo.findByTareaId(tarea.getId());
        boolean notificado = false;
        for (AsignacionTarea a : asignaciones) {
            if (a.getAsignadoPor() != null) {
                notifRepo.save(Notificacion.builder()
                    .destinatario(a.getAsignadoPor())
                    .mensaje(mensaje)
                    .tipo(tipo)
                    .urlDestino("/tareas/detalle/" + tarea.getId())
                    .build());
                notificado = true;
            }
        }
        if (!notificado) {
            usuarioRepo.findActivosByRol(Rol.ADMINISTRADOR).forEach(admin ->
                notifRepo.save(Notificacion.builder()
                    .destinatario(admin)
                    .mensaje(mensaje)
                    .tipo(tipo)
                    .urlDestino("/tareas/detalle/" + tarea.getId())
                    .build())
            );
        }
    }

    @Transactional
    public void cambiarPrioridad(Long idTarea, Tarea.Prioridad prioridad, Usuario responsable) {
        Tarea tarea = buscarPorId(idTarea);
        tarea.setPrioridad(prioridad);
        tareaRepo.save(tarea);
    }

    public List<AsignacionTarea> misTareas(Long idEmpleado) {
        return asignacionRepo.findByEmpleadoId(idEmpleado);
    }

    public List<AsignacionTarea> todasLasAsignaciones() {
        return asignacionRepo.findAll();
    }

    public List<ComentarioTarea> comentarios(Long idTarea) {
        return comentarioRepo.findByTareaIdOrderByFechaComentarioAsc(idTarea);
    }

    public Tarea buscarPorId(Long id) {
        return tareaRepo.findById(id)
            .orElseThrow(() -> new BusinessException("Tarea no encontrada: " + id));
    }

    public double calcularRendimiento(Long idEmpleado) {
        long total = asignacionRepo.countTotalByEmpleado(idEmpleado);
        if (total == 0) return 0.0;
        long comp = asignacionRepo.countCompletadasByEmpleado(idEmpleado);
        return Math.round(comp * 100.0 / total * 10.0) / 10.0;
    }
    public AsignacionTarea asignacionPorTarea(Long idTarea) {
    return asignacionRepo.findByTareaId(idTarea)
        .stream().findFirst().orElse(null);
}

}
