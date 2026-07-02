package com.acmafer.comun.controlador;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest req, Model model) {
        Object status = req.getAttribute("jakarta.servlet.error.status_code");
        int code = status != null ? (int) status : 500;
        model.addAttribute("codigo", code);
        model.addAttribute("mensaje", getMensaje(code));
        return switch (code) {
            case 403 -> "error/403";
            case 404 -> "error/404";
            default -> "error/500";
        };
    }

    private String getMensaje(int code) {
        return switch (code) {
            case 403 -> "No tienes permisos para acceder a esta página.";
            case 404 -> "La página que buscas no existe o fue movida.";
            default -> "Ocurrió un error inesperado en el servidor.";
        };
    }
}
