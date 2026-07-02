package com.acmafer.comun.controlador;

import com.acmafer.comun.seguridad.CustomOAuth2User;
import com.acmafer.modulos.notificaciones.repositorio.NotificacionRepository;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Inyecta atributos globales en todos los modelos de Thymeleaf.
 * Así el layout siempre tiene acceso al ID del usuario autenticado
 * y al contador de notificaciones sin depender de cada controlador
 * por separado.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final NotificacionRepository notificacionRepo;

    @ModelAttribute("currentUserId")
    public String currentUserId() {
        Usuario usuario = usuarioActual();
        return usuario != null ? String.valueOf(usuario.getId()) : "anonimo";
    }

    /**
     * Contador global de notificaciones no leídas.
     * Disponible en TODAS las vistas como ${notifCount} sin que
     * cada controlador tenga que agregarlo manualmente.
     */
    @ModelAttribute("notifCount")
    public long notifCount() {
        Usuario usuario = usuarioActual();
        if (usuario == null) return 0;
        try {
            return notificacionRepo.countByDestinatarioIdAndLeidaFalse(usuario.getId());
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Helper: resuelve el Usuario autenticado sin importar si es LOCAL u OAuth2 ──
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUsuario();
        }
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        return null;
    }
}