package com.acmafer.modulos.tareas.controlador;


import com.acmafer.modulos.tareas.servicio.TareaService;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/rendimiento")
@RequiredArgsConstructor
public class RendimientoController {

    private final TareaService tareaService;
    private final UsuarioService usuarioService;

    @GetMapping({"", "/"})
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String equipo(Model model) {
        List<Usuario> trabajadores = usuarioService.listarTrabajadoresActivos();
        var datos = trabajadores.stream()
            .map(t -> new Object[]{t, tareaService.calcularRendimiento(t.getId())})
            .toList();
        model.addAttribute("rendimientos", datos);
        return "rendimiento/equipo";
    }

    @GetMapping("/mi-rendimiento")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public String personal(@AuthenticationPrincipal Usuario u, Model model) {
        model.addAttribute("rendimiento", tareaService.calcularRendimiento(u.getId()));
        model.addAttribute("misTareas", tareaService.misTareas(u.getId()));
        return "rendimiento/personal";
    }
}
