package com.acmafer.modulos.usuarios.controlador;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.modulos.usuarios.dto.UsuarioRegistroDTO;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.servicio.PasswordService;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final PasswordService passwordService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String expired,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {
        if (usuario != null)
            return "redirect:/dashboard";
        if (error != null) {
            String mensaje = switch (error) {
                case "inactiva" -> "Tu cuenta está inactiva. Contacta al administrador.";
                case "bloqueada" -> "Tu cuenta está bloqueada por intentos fallidos. Contacta al administrador.";
                default -> "Correo o contraseña incorrectos";
            };
            model.addAttribute("error", mensaje);
        }
        if (expired != null)
            model.addAttribute("error", "Tu sesión ha expirado");
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registroPage(Model model) {
        model.addAttribute("dto", new UsuarioRegistroDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("dto") UsuarioRegistroDTO dto,
            BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors())
            return "auth/registro";
        try {
            usuarioService.registrar(dto);
            ra.addFlashAttribute("exito", "Cuenta creada. Ya puedes iniciar sesión.");
            return "redirect:/auth/login";
        } catch (BusinessException e) {
            result.rejectValue("email", "error", e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/recuperar")
    public String recuperarPage() {
        return "auth/recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitarRecuperacion(@RequestParam String email, RedirectAttributes ra) {
        try {
            passwordService.solicitarRecuperacion(email);
            ra.addFlashAttribute("info", "Se envió el código a tu correo.");
            return "redirect:/auth/recuperar/codigo";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/recuperar";
        }
    }

    @GetMapping("/recuperar/codigo")
    public String codigoPage() {
        return "auth/verificar-codigo";
    }

    @PostMapping("/recuperar/nueva-clave")
    public String nuevaClave(@RequestParam String token,
            @RequestParam String clave, RedirectAttributes ra) {
        try {
            passwordService.cambiarConCodigo(token, clave);
            ra.addFlashAttribute("exito", "Contraseña restablecida. Ya puedes iniciar sesión.");
            return "redirect:/auth/login";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/recuperar/codigo";
        }
    }
}
