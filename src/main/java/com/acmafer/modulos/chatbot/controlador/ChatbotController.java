package com.acmafer.modulos.chatbot.controlador;

import com.acmafer.modulos.chatbot.servicio.ChatbotService;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * Endpoint del chatbot — accesible para autenticados Y visitantes públicos.
     * Si no hay sesión activa se asigna ROLE_ANONIMO y solo se expone info pública.
     */
    @PostMapping("/responder")
    public ResponseEntity<Map<String, String>> responder(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal Usuario usuario) {

        String mensaje = payload.getOrDefault("mensaje", "").trim();
        if (mensaje.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("respuesta", "Por favor escribe tu pregunta 😊"));
        }

        String rol = (usuario != null)
                ? "ROLE_" + usuario.getRol().name()
                : "ROLE_ANONIMO";

        return ResponseEntity.ok(Map.of("respuesta", chatbotService.responder(mensaje, rol)));
    }
}