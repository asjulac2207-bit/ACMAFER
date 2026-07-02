package com.acmafer.modulos.usuarios.controlador;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.modulos.usuarios.dto.UsuarioRegistroDTO;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("pagina",
                usuarioService.listar(q, PageRequest.of(page, 15, Sort.by("nombre"))));
        model.addAttribute("filtroQ", q);
        model.addAttribute("roles", Rol.values());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("dto", new UsuarioRegistroDTO());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esNuevo", true);
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String editarForm(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setDocumento(usuario.getDocumento());
        dto.setCelular(usuario.getCelular());
        model.addAttribute("dto", dto);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esNuevo", false);
        return "usuarios/form";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String actualizar(@PathVariable Long id,
            @Valid @ModelAttribute("dto") UsuarioRegistroDTO dto,
            BindingResult result,
            @RequestParam Rol rol,
            RedirectAttributes ra, Model model) {

        // Validación manual: contraseña NO es obligatoria al editar
        // Solo valida longitud si se proporcionó una contraseña
        if (dto.getClave() != null && !dto.getClave().isBlank() && dto.getClave().length() < 8) {
            result.rejectValue("clave", "Size", "Mínimo 8 caracteres");
        }

        if (result.hasErrors()) {
            model.addAttribute("esNuevo", false);
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            model.addAttribute("roles", Rol.values());
            return "usuarios/form";
        }

        try {
            usuarioService.actualizar(id, dto, rol);
            ra.addFlashAttribute("exito", "Usuario actualizado exitosamente");
            return "redirect:/usuarios";
        } catch (BusinessException e) {
            result.rejectValue("email", "error", e.getMessage());
            model.addAttribute("esNuevo", false);
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            model.addAttribute("roles", Rol.values());
            return "usuarios/form";
        }
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("dto") UsuarioRegistroDTO dto,
            BindingResult result,
            @RequestParam Rol rol,
            RedirectAttributes ra, Model model) {

        // Validación manual: contraseña SÍ es obligatoria al crear
        if (dto.getClave() == null || dto.getClave().isBlank()) {
            result.rejectValue("clave", "NotBlank", "La contraseña es obligatoria");
        } else if (dto.getClave().length() < 8) {
            result.rejectValue("clave", "Size", "Mínimo 8 caracteres");
        }

        if (result.hasErrors()) {
            model.addAttribute("esNuevo", true);
            model.addAttribute("roles", Rol.values());
            return "usuarios/form";
        }

        try {
            usuarioService.registrarPorAdmin(dto, rol);
            ra.addFlashAttribute("exito", "Usuario creado exitosamente");
            return "redirect:/usuarios";
        } catch (BusinessException e) {
            result.rejectValue("email", "error", e.getMessage());
            model.addAttribute("esNuevo", true);
            model.addAttribute("roles", Rol.values());
            return "usuarios/form";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id,
            @RequestParam String estado,
            RedirectAttributes ra) {
        usuarioService.cambiarEstado(id, estado);
        ra.addFlashAttribute("exito", "Estado actualizado");
        return "redirect:/usuarios";
    }
}