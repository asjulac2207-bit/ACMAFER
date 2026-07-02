package com.acmafer.modulos.notificaciones.controlador;


import com.acmafer.modulos.notificaciones.repositorio.NotificacionRepository;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionRepository notifRepo;

    @GetMapping
    public String listar(@AuthenticationPrincipal Usuario u, Model model) {
        model.addAttribute("notificaciones",
            notifRepo.findByDestinatarioIdOrderByFechaCreacionDesc(
                u.getId(), PageRequest.of(0, 30)));
        model.addAttribute("titulo", "Notificaciones");
        return "notificaciones/lista";
    }

    @PostMapping("/{id}/marcar-leida")
    @ResponseBody
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id,
                                             @AuthenticationPrincipal Usuario u) {
        notifRepo.findById(id).ifPresent(n -> {
            if (n.getDestinatario().getId().equals(u.getId())) {
                n.setLeida(true);
                notifRepo.save(n);
            }
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/marcar-todas")
    public String marcarTodas(@AuthenticationPrincipal Usuario u) {
        notifRepo.marcarTodasLeidas(u.getId());
        return "redirect:/notificaciones";
    }
}
