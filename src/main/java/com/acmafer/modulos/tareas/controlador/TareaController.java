package com.acmafer.modulos.tareas.controlador;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.modulos.tareas.dto.TareaDTO;
import com.acmafer.modulos.tareas.entidad.Tarea;
import com.acmafer.modulos.tareas.servicio.TareaService;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;
    private final UsuarioService usuarioService;

    @GetMapping("/mis-tareas")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public String misTareas(@AuthenticationPrincipal Usuario u, Model model) {
        model.addAttribute("asignaciones", tareaService.misTareas(u.getId()));
        model.addAttribute("rendimiento", tareaService.calcularRendimiento(u.getId()));
        return "tareas/mis-tareas";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String admin(Model model) {
        model.addAttribute("asignaciones", tareaService.todasLasAsignaciones());
        model.addAttribute("trabajadores", usuarioService.listarTrabajadoresActivos());
        return "tareas/admin";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String nuevaForm(Model model) {
        model.addAttribute("dto", new TareaDTO());
        model.addAttribute("trabajadores", usuarioService.listarTrabajadoresActivos());
        return "tareas/form";
    }

    @PostMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String crear(@Valid @ModelAttribute("dto") TareaDTO dto,
            BindingResult result,
            @AuthenticationPrincipal Usuario u,
            RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trabajadores", usuarioService.listarTrabajadoresActivos());
            return "tareas/form";
        }
        try {
            tareaService.crear(dto, u);
            ra.addFlashAttribute("exito", "Tarea creada exitosamente");
            return "redirect:/tareas/admin";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/tareas/nueva";
        }
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
            @RequestParam String nuevoEstado,
            @AuthenticationPrincipal Usuario u,
            RedirectAttributes ra) {
        try {
            tareaService.cambiarEstado(id, Tarea.Estado.valueOf(nuevoEstado), u);
            ra.addFlashAttribute("exito", "Estado actualizado");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        Rol rol = u.getRol();
        return (rol == Rol.TRABAJADOR)
                ? "redirect:/tareas/mis-tareas"
                : "redirect:/tareas/admin";
    }

    @PostMapping("/comentar/{id}")
    public String comentar(@PathVariable Long id,
            @RequestParam String contenido,
            @AuthenticationPrincipal Usuario u,
            RedirectAttributes ra) {
        tareaService.agregarComentario(id, contenido, u);
        ra.addFlashAttribute("exito", "Comentario agregado");
        return "redirect:/tareas/detalle/" + id;
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("tarea", tareaService.buscarPorId(id));
        model.addAttribute("comentarios", tareaService.comentarios(id));
         model.addAttribute("asignacion", tareaService.asignacionPorTarea(id));
    return "tareas/detalle";
        
    }

    @GetMapping
    public String index(@AuthenticationPrincipal Usuario u) {
        Rol rol = u.getRol();
        if (rol == Rol.TRABAJADOR) {
            return "redirect:/tareas/mis-tareas";
        }
        return "redirect:/tareas/admin";
    }
}
