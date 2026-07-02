package com.acmafer.comun.servicio;

import com.acmafer.modulos.tareas.entidad.Tarea;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import java.util.List;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${acmafer.mail.from}")
    private String fromAddress;

    // ─── Recuperación de contraseña ───────────────────────────────────

    @Async
    public void enviarRecuperacion(String dest, String codigo) {
        String asunto = "🔐 Código de recuperación — ACMAFER";
        String cuerpo = buildOtpHtml(
                "Recuperación de contraseña",
                "Recibimos una solicitud para restablecer tu contraseña. Usa el código de 6 dígitos a continuación:",
                codigo,
                "Este código expira en <strong>30 minutos</strong>. Si no solicitaste esto, ignora este mensaje.");
        enviar(dest, asunto, cuerpo);
    }

    @Async
    public void enviarTokenRecuperacion(Usuario usuario, String codigo) {
        enviarRecuperacion(usuario.getEmail(), codigo);
    }

    // ─── Bienvenida ───────────────────────────────────────────────────

    @Async
    public void enviarBienvenida(Usuario usuario) {
        enviarBienvenida(usuario.getEmail(), usuario.getNombreCompleto());
    }

    @Async
    public void enviarBienvenida(String dest, String nombre) {
        String asunto = "👋 Bienvenido/a a ACMAFER";
        String cuerpo = buildInfoHtml(
                "¡Bienvenido/a, " + nombre + "!",
                "Tu cuenta en el sistema ACMAFER ha sido creada exitosamente.",
                "Ya puedes iniciar sesión con tu correo y la contraseña asignada. Si tienes alguna duda, contacta al administrador.",
                "#28a745");
        enviar(dest, asunto, cuerpo);
    }

    // ─── Notificación genérica ────────────────────────────────────────

    @Async
    public void enviarNotificacion(String dest, String asunto, String cuerpo) {
        String html = buildInfoHtml("Notificación ACMAFER", asunto, cuerpo, "#0d6efd");
        enviar(dest, asunto, html);
    }

    // ─── Cuenta bloqueada ─────────────────────────────────────────────

    @Async
    public void notificarCuentaBloqueada(Usuario usuario) {
        String asunto = "⚠️ Tu cuenta ha sido bloqueada — ACMAFER";
        String cuerpo = buildInfoHtml(
                "Cuenta bloqueada",
                "Tu cuenta ha sido bloqueada por múltiples intentos fallidos de inicio de sesión.",
                "Si crees que esto es un error, comunícate con el administrador del sistema para desbloquearla.",
                "#dc3545");
        enviar(usuario.getEmail(), asunto, cuerpo);
    }

    // ─── Cambio de estado de pedido (CORREGIDO: recibe datos primitivos) ────

    @Async
    public void notificarCambioEstadoPedido(String emailCliente, String numeroPedido,
            String estadoAnterior, String nuevoEstado) {
        String emoji = estadoEmoji(nuevoEstado);
        String asunto = emoji + " Tu pedido " + numeroPedido + " — " + nuevoEstado;

        String mensaje = "El estado de tu pedido <strong>" + numeroPedido +
                "</strong> ha cambiado de <em>" + estadoAnterior + "</em> a <strong>" + nuevoEstado + "</strong>.";

        String detalle = switch (nuevoEstado) {
            case "Procesando" -> "Tu pedido está siendo preparado. Te notificaremos cuando sea enviado.";
            case "Enviado" -> "Tu pedido está en camino. Pronto llegará a tu dirección de entrega.";
            case "Entregado" -> "¡Tu pedido ha sido entregado! Gracias por confiar en ACMAFER.";
            case "Cancelado" -> "Tu pedido ha sido cancelado. Contacta al soporte si tienes dudas.";
            default -> "Revisa el detalle de tu pedido en la plataforma.";
        };

        String cuerpo = buildInfoHtml("Actualización de pedido", mensaje, detalle, colorEstado(nuevoEstado));
        enviar(emailCliente, asunto, cuerpo);
        log.info("[EMAIL] Notificación de cambio de estado enviada a {}", emailCliente);
    }

    // ─── Asignación de tarea al trabajador + al administrador ─────────

    @Async
    public void notificarAsignacionTarea(Usuario empleado, Tarea tarea, Usuario asignadoPor) {
        // 1. Al trabajador
        String asuntoTrabajador = "📋 Nueva tarea asignada — " + tarea.getTitulo();
        String mensajeTrabajador = "Se te ha asignado una nueva tarea en el sistema ACMAFER.";
        String detalleTrabajador = "<strong>Tarea:</strong> " + tarea.getTitulo() + "<br/>" +
                "<strong>Asignada por:</strong> " + asignadoPor.getNombreCompleto() + "<br/>" +
                "<strong>Prioridad:</strong> " + tarea.getPrioridad() + "<br/>" +
                (tarea.getDescripcion() != null ? "<strong>Descripción:</strong> " + tarea.getDescripcion() : "") +
                "<br/><br/>Ingresa a la plataforma para ver los detalles y comenzar a trabajar.";
        String cuerpoTrabajador = buildInfoHtml("Nueva tarea asignada", mensajeTrabajador, detalleTrabajador,
                "#0d6efd");
        enviar(empleado.getEmail(), asuntoTrabajador, cuerpoTrabajador);

        // 2. Al administrador (quien asignó)
        String asuntoAdmin = "✅ Tarea asignada a " + empleado.getNombreCompleto();
        String mensajeAdmin = "Has asignado exitosamente la tarea <strong>" + tarea.getTitulo() + "</strong>.";
        String detalleAdmin = "<strong>Asignado a:</strong> " + empleado.getNombreCompleto() + " ("
                + empleado.getEmail() + ")<br/>" +
                "<strong>Prioridad:</strong> " + tarea.getPrioridad();
        String cuerpoAdmin = buildInfoHtml("Confirmación de asignación", mensajeAdmin, detalleAdmin, "#198754");
        enviar(asignadoPor.getEmail(), asuntoAdmin, cuerpoAdmin);
    }

    // ─── Cambio de estado de tarea (notifica al trabajador y al admin) ─

    @Async
    public void notificarCambioEstadoTarea(Tarea tarea, Tarea.Estado estadoAnterior,
            Usuario trabajador, List<Usuario> admins) {
        String estadoNuevo = tarea.getEstado().name();
        String estadoAnt = estadoAnterior.name();

        // CORREGIDO: Extraer el estado a una variable para usar en switch
        Tarea.Estado estadoActual = tarea.getEstado();
        String emoji = switch (estadoActual) {
            case NO_INICIADA -> "📋";
            case EN_PROGRESO -> "⚙️";
            case BLOQUEADA -> "🚫";
            case COMPLETADA -> "✅";
            case CANCELADA -> "❌";
        };

        // 1. Notificar al trabajador asignado
        if (trabajador != null && trabajador.getEmail() != null) {
            String asunto = emoji + " Estado de tarea actualizado — " + tarea.getTitulo();
            String mensaje = "El estado de tu tarea <strong>" + tarea.getTitulo()
                    + "</strong> ha cambiado de <em>" + estadoAnt
                    + "</em> a <strong>" + estadoNuevo + "</strong>.";

            String detalle = switch (estadoActual) {
                case NO_INICIADA -> "La tarea está pendiente de iniciar. Revisa los detalles en la plataforma.";
                case EN_PROGRESO -> "La tarea está en progreso. Recuerda actualizarla cuando la completes.";
                case BLOQUEADA -> "La tarea ha sido bloqueada. Contacta al administrador si tienes dudas.";
                case COMPLETADA -> "¡Excelente trabajo! La tarea ha sido marcada como completada.";
                case CANCELADA -> "La tarea ha sido cancelada. Contacta al administrador si tienes dudas.";
            };

            String cuerpo = buildInfoHtml("Actualización de tarea", mensaje, detalle, "#0d6efd");
            enviar(trabajador.getEmail(), asunto, cuerpo);
        }

        // 2. Notificar a los administradores
        for (Usuario admin : admins) {
            if (admin.getEmail() == null)
                continue;
            String asuntoAdmin = emoji + " Tarea actualizada: " + tarea.getTitulo();
            String mensajeAdmin = "La tarea <strong>" + tarea.getTitulo()
                    + "</strong> cambió de estado: <em>" + estadoAnt
                    + "</em> → <strong>" + estadoNuevo + "</strong>.";
            String detalleAdmin = "<strong>Trabajador:</strong> "
                    + (trabajador != null ? trabajador.getNombreCompleto() : "Sin asignar")
                    + "<br/><strong>Prioridad:</strong> " + tarea.getPrioridad();
            String cuerpoAdmin = buildInfoHtml("Cambio de estado de tarea", mensajeAdmin, detalleAdmin, "#6f42c1");
            enviar(admin.getEmail(), asuntoAdmin, cuerpoAdmin);
        }
    }

    // ─── Motor de envío ───────────────────────────────────────────────

    private void enviar(String dest, String asunto, String htmlCuerpo) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress, "ACMAFER Sistema");
            helper.setTo(dest);
            helper.setSubject(asunto);
            helper.setText(htmlCuerpo, true);
            mailSender.send(msg);
            log.info("[EMAIL ✓] Para: {} | Asunto: {}", dest, asunto);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[EMAIL ✗] No se pudo enviar a {}: {}", dest, e.getMessage());
        }
    }

    // ─── Helpers de plantilla ────────────────────────────────────────

    private String estadoEmoji(String estado) {
        return switch (estado) {
            case "Procesando" -> "⚙️";
            case "Enviado" -> "🚚";
            case "Entregado" -> "✅";
            case "Cancelado" -> "❌";
            default -> "📦";
        };
    }

    private String colorEstado(String estado) {
        return switch (estado) {
            case "Procesando" -> "#fd7e14";
            case "Enviado" -> "#0dcaf0";
            case "Entregado" -> "#198754";
            case "Cancelado" -> "#dc3545";
            default -> "#6c757d";
        };
    }

    /** Plantilla OTP con código grande destacado */
    private String buildOtpHtml(String titulo, String intro, String codigo, String nota) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:30px 0;">
                    <tr><td align="center">
                      <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
                        <tr><td style="background:#1a2340;padding:28px 32px;text-align:center;">
                          <span style="font-size:28px;font-weight:900;color:#ffffff;letter-spacing:2px;">ACM<span style="color:#e8630a;">AFER</span></span>
                        </td></tr>
                        <tr><td style="padding:32px 40px;">
                          <h2 style="color:#1a2340;margin:0 0 12px;">%s</h2>
                          <p style="color:#555;font-size:15px;line-height:1.6;margin:0 0 24px;">%s</p>
                          <div style="background:#f0f4ff;border-radius:10px;padding:24px;text-align:center;margin-bottom:24px;">
                            <span style="font-size:42px;font-weight:900;letter-spacing:16px;color:#1a2340;font-family:monospace;">%s</span>
                          </div>
                          <p style="color:#888;font-size:13px;line-height:1.5;margin:0;">%s</p>
                        </td></tr>
                        <tr><td style="background:#f8f9fa;padding:16px 40px;text-align:center;">
                          <p style="color:#aaa;font-size:12px;margin:0;">ACMAFER © 2026 — No respondas este correo.</p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body></html>
                """
                .formatted(titulo, intro, codigo, nota);
    }

    /** Plantilla informativa general */
    private String buildInfoHtml(String titulo, String intro, String detalle, String accentColor) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:30px 0;">
                    <tr><td align="center">
                      <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
                        <tr><td style="background:#1a2340;padding:28px 32px;text-align:center;">
                          <span style="font-size:28px;font-weight:900;color:#ffffff;letter-spacing:2px;">ACM<span style="color:#e8630a;">AFER</span></span>
                        </td></tr>
                        <tr><td style="padding:8px 40px 0;border-top:4px solid %s;"></td></tr>
                        <tr><td style="padding:28px 40px;">
                          <h2 style="color:#1a2340;margin:0 0 12px;">%s</h2>
                          <p style="color:#333;font-size:15px;line-height:1.6;margin:0 0 16px;">%s</p>
                          <p style="color:#555;font-size:14px;line-height:1.6;margin:0;">%s</p>
                        </td></tr>
                        <tr><td style="background:#f8f9fa;padding:16px 40px;text-align:center;">
                          <p style="color:#aaa;font-size:12px;margin:0;">ACMAFER © 2026 — No respondas este correo.</p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body></html>
                """
                .formatted(accentColor, titulo, intro, detalle);
    }
}