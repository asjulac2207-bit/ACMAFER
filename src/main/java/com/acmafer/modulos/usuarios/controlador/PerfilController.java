package com.acmafer.modulos.usuarios.controlador;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.seguridad.CustomOAuth2User;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import com.acmafer.modulos.usuarios.servicio.PasswordService;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;


@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PasswordService passwordService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    /**
     * Resuelve el Usuario real sin importar si el principal es Usuario o
     * CustomOAuth2User
     */
    private Usuario resolverUsuario(Object principal) {
        if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUsuario();
        }
        if (principal instanceof Usuario u) {
            return u;
        }
        throw new BusinessException("Sesión inválida");
    }

    @GetMapping({ "", "/" })
    public String perfil(@AuthenticationPrincipal Object principal, Model model) {
        Usuario u = resolverUsuario(principal);
        boolean esGoogle = "GOOGLE".equals(u.getProveedor());

        String avatarUrlM = "https://tse3.mm.bing.net/th/id/OIP.R4iLiCt_79HCuuJHeUgf_wHaHa?rs=1&pid=ImgDetMain&o=7&rm=3";
        String avatarUrlF = "https://img.freepik.com/premium-vector/brown-skin-woman-with-long-hair-dress-portrait-cartoon-illustration_661611-543.jpg";

        // Lógica de prioridad de avatar
        String fotoGoogle = (esGoogle && Boolean.TRUE.equals(u.getUsarFotoGoogle())) ? u.getFotoUrl() : null;
        String avatarUrl = "";
        if (fotoGoogle == null || fotoGoogle.isBlank()) {
            if ("M".equals(u.getGenero()))
                avatarUrl = avatarUrlM;
            else if ("F".equals(u.getGenero()))
                avatarUrl = avatarUrlF;
        }

        model.addAttribute("usuario", u);
        model.addAttribute("fotoGoogle", fotoGoogle);
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("esGoogle", esGoogle);
        model.addAttribute("usarFotoGoogle", Boolean.TRUE.equals(u.getUsarFotoGoogle()));

        return "perfil/index";
    }

    @GetMapping("/cambiar-contrasena")
    public String cambiarForm(@AuthenticationPrincipal Object principal) {
        Usuario u = resolverUsuario(principal);
        // Usuarios Google no tienen contraseña local
        if ("GOOGLE".equals(u.getProveedor())) {
            return "redirect:/perfil";
        }
        return "perfil/cambiar-contrasena";
    }

    @PostMapping("/cambiar-contrasena")
    public String cambiar(@RequestParam String claveActual,
            @RequestParam String claveNueva,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario u = resolverUsuario(principal);
        if ("GOOGLE".equals(u.getProveedor())) {
            return "redirect:/perfil";
        }
        try {
            passwordService.cambiarContrasena(u, claveActual, claveNueva);
            ra.addFlashAttribute("exito", "Contraseña actualizada exitosamente.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/perfil/cambiar-contrasena";
        }
        return "redirect:/perfil";
    }

    @PostMapping("/actualizar-genero")
    public String actualizarGenero(@RequestParam String genero,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario u = resolverUsuario(principal);
        u.setGenero(genero);
        usuarioRepository.save(u);
        ra.addFlashAttribute("exito", "Avatar actualizado correctamente.");
        return "redirect:/perfil";
    }

    @PostMapping("/actualizar-datos")
    public String actualizarDatos(@RequestParam(required = false) String celular,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario u = resolverUsuario(principal);
        boolean esGoogle = "GOOGLE".equals(u.getProveedor());

        if (celular != null && !celular.isBlank())
            u.setCelular(celular.trim());

        if (!esGoogle) {
            if (nombre != null && !nombre.isBlank())
                u.setNombre(nombre.trim());
            if (apellido != null && !apellido.isBlank())
                u.setApellido(apellido.trim());
            if (documento != null && !documento.isBlank())
                u.setDocumento(documento.trim());
        } else {
            if (documento != null && !documento.isBlank()
                    && u.getDocumento().startsWith("GOOGLE-"))
                u.setDocumento(documento.trim());
        }

        usuarioRepository.save(u);
        ra.addFlashAttribute("exito", "Datos actualizados correctamente.");
        return "redirect:/perfil";
    }

    @PostMapping("/actualizar-avatar")
    public String actualizarAvatar(@RequestParam String genero,
            @RequestParam(required = false) String usarFotoGoogle,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario u = resolverUsuario(principal);
        u.setGenero(genero);
        if ("GOOGLE".equals(u.getProveedor())) {
            u.setUsarFotoGoogle("google".equals(usarFotoGoogle));
        }
        usuarioRepository.save(u);
        ra.addFlashAttribute("exito", "Avatar actualizado correctamente.");
        return "redirect:/perfil";
    }
}
