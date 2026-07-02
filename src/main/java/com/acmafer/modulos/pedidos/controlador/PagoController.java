package com.acmafer.modulos.pedidos.controlador;

import com.acmafer.comun.configuracion.EpaycoConfig;
import com.acmafer.modulos.pedidos.servicio.PagoService;
import com.acmafer.modulos.pedidos.servicio.PedidoService;
import com.acmafer.modulos.usuarios.entidad.Usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
@RequestMapping("/pedidos/pago")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

    private final PedidoService pedidoService;
    private final PagoService pagoService;
    private final EpaycoConfig epaycoConfig;

   @GetMapping("/respuesta")
public String respuesta(
        @RequestParam(required = false) String ref_payco,
        @RequestParam(required = false) String x_ref_payco,
        @RequestParam(required = false) String simulado,
        @RequestParam(required = false) String numeroPedido,   
        @RequestParam(required = false) String estado,         
        @AuthenticationPrincipal Object principal,
        Model model) {

    if ("true".equals(simulado) && numeroPedido != null) {
        model.addAttribute("estado",       estado != null ? estado : "Aceptada");
        model.addAttribute("numeroPedido", numeroPedido);
        model.addAttribute("mensaje",      "¡Tu pago fue procesado exitosamente!");
        model.addAttribute("simulado",     true);
        return "pedidos/pago-resultado";
    }

    String referencia = ref_payco != null ? ref_payco : x_ref_payco;
    log.info("Respuesta ePayco recibida - ref: {}", referencia);
    if (referencia == null) {
        model.addAttribute("estado",  "error");
        model.addAttribute("mensaje", "No se recibió referencia de pago");
        return "pedidos/pago-resultado";
    }
    try {
        Map<String, Object> resultado = pagoService.verificarPago(referencia);
        String estadoPago = (String) resultado.get("estado");
        String numeroPedidoConsultado = (String) resultado.get("numeroPedido"); 
        model.addAttribute("estado",       estadoPago);
        model.addAttribute("referencia",   referencia);
        model.addAttribute("numeroPedido", numeroPedidoConsultado);
        model.addAttribute("resultado",    resultado);
        if ("Aceptada".equals(estadoPago) || "aprobado".equalsIgnoreCase(estadoPago)) {
            model.addAttribute("mensaje", "¡Tu pago fue procesado exitosamente!");
        } else if ("Pendiente".equals(estadoPago)) {
            model.addAttribute("mensaje", "Tu pago está en proceso de verificación.");
        } else {
            model.addAttribute("mensaje", "El pago no pudo completarse. Intenta de nuevo.");
        }
    } catch (Exception e) {
        log.error("Error verificando pago: {}", e.getMessage());
        model.addAttribute("estado",  "error");
        model.addAttribute("mensaje", "Error al verificar el pago.");
    }
    return "pedidos/pago-resultado";
}

    @PostMapping("/confirmacion")
    @ResponseBody
    public String confirmacion(@RequestParam Map<String, String> params) {
        log.info("Confirmación ePayco recibida: {}", params);
        try {
            pagoService.procesarConfirmacion(params);
            return "OK";
        } catch (Exception e) {
            log.error("Error procesando confirmación ePayco: {}", e.getMessage());
            return "ERROR";
        }
    }
}
