package com.acmafer.modulos.pedidos.servicio;

import com.acmafer.comun.configuracion.EpaycoConfig;
import com.acmafer.modulos.pedidos.entidad.DireccionEntrega;
import com.acmafer.modulos.pedidos.entidad.Pedido;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final EpaycoConfig epaycoConfig;
    private final PedidoRepository pedidoRepo;
    private final ObjectMapper objectMapper;

  public Map<String, Object> verificarPago(String refPayco) throws Exception {
    String url = "https://secure.epayco.co/validation/v1/reference/" + refPayco;

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

    JsonNode root = objectMapper.readTree(response.getBody());

        Map<String, Object> resultado = new HashMap<>();
        if (root.has("data")) {
            JsonNode data = root.get("data");
            String estado        = data.path("x_transaction_state").asText();
            String numeroPedido  = data.path("x_extra1").asText(); 
            String monto         = data.path("x_amount").asText();
            String moneda        = data.path("x_currency_code").asText();
            String fechaPago     = data.path("x_transaction_date").asText();

            resultado.put("estado", estado);
            resultado.put("numeroPedido", numeroPedido);
            resultado.put("monto", monto);
            resultado.put("moneda", moneda);
            resultado.put("fechaPago", fechaPago);
            resultado.put("refPayco", refPayco);
        } else {
            log.warn("Respuesta de ePayco sin campo 'data' para ref {}: {}", refPayco, response.getBody());
        }
        return resultado;
    }

    
    @Transactional
    public void procesarConfirmacion(Map<String, String> params) throws Exception {
        String xRefPayco         = params.getOrDefault("x_ref_payco", "");
        String xTransactionId    = params.getOrDefault("x_transaction_id", "");
        String xAmount           = params.getOrDefault("x_amount", "");
        String xCurrencyCode     = params.getOrDefault("x_currency_code", "");
        String xSignature        = params.getOrDefault("x_signature", "");
        String xTransactionState = params.getOrDefault("x_transaction_state", "");
        String xExtra1           = params.getOrDefault("x_extra1", ""); 

        if (!validarFirma(xRefPayco, xTransactionId, xAmount, xCurrencyCode, xSignature)) {
            log.warn("Firma ePayco inválida para ref: {} (pedido {})", xRefPayco, xExtra1);
            return;
        }

        Pedido pedido = pedidoRepo.findByNumeroPedido(xExtra1).orElse(null);
        if (pedido == null) {
            log.warn("Confirmación ePayco para un pedido que no existe en BD: {}", xExtra1);
            return;
        }

        try {
            BigDecimal montoConfirmado = new BigDecimal(xAmount);
            if (montoConfirmado.compareTo(pedido.getTotal()) != 0) {
                log.warn("Monto ePayco ({}) no coincide con el total del pedido {} ({})",
                        montoConfirmado, xExtra1, pedido.getTotal());
                return;
            }
        } catch (NumberFormatException e) {
            log.warn("x_amount inválido en confirmación ePayco: '{}'", xAmount);
            return;
        }

        String nuevoEstado = mapearEstadoEpayco(xTransactionState);
        if (nuevoEstado != null && !nuevoEstado.equals(pedido.getEstado())) {
            pedido.setEstado(nuevoEstado);
            pedidoRepo.save(pedido);
            log.info("Pedido {} actualizado a estado: {}", xExtra1, nuevoEstado);
        }
    }

    
    private boolean validarFirma(String refPayco, String transactionId,
                                  String amount, String currency, String firmaRecibida) {
        try {
            String cadena = epaycoConfig.getCustomerId() + "^" + epaycoConfig.getPKey()
                    + "^" + refPayco + "^" + transactionId
                    + "^" + amount + "^" + currency;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cadena.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            boolean valida = hexString.toString().equals(firmaRecibida);
            if (!valida) {
                log.debug("Firma calculada: {} | Firma recibida: {} | Cadena base: {}",
                        hexString, firmaRecibida, cadena.replace(epaycoConfig.getPKey(), "***"));
            }
            return valida;
        } catch (Exception e) {
            log.error("Error validando firma ePayco: {}", e.getMessage());
            return false;
        }
    }

    
    private String mapearEstadoEpayco(String estadoEpayco) {
        return switch (estadoEpayco.toLowerCase()) {
            case "aceptada", "aprobado"  -> PedidoService.PROCESANDO;
            case "pendiente"             -> PedidoService.PENDIENTE;
            case "rechazada", "fallida", "expirada" -> PedidoService.CANCELADO;
            default -> null;
        };
    }

  public Map<String, Object> generarDatosEpayco(Pedido pedido, DireccionEntrega direccion) {
    Map<String, Object> datos = new HashMap<>();
    datos.put("name",              "Pedido ACMAFER " + pedido.getNumeroPedido());
    datos.put("description",       "Pedido ACMAFER " + pedido.getNumeroPedido());
    datos.put("invoice",           pedido.getNumeroPedido());
    datos.put("currency",          "cop");
    datos.put("amount",            pedido.getTotal().toPlainString());
    datos.put("tax_base",          pedido.getTotal().toPlainString());
    datos.put("tax",               "0");
    datos.put("country",           "co");
    datos.put("lang",              "es");
    datos.put("external",          "false");
    datos.put("extra1",            pedido.getNumeroPedido()); 

    datos.put("response",          epaycoConfig.getResponseUrl());
    datos.put("confirmation",      epaycoConfig.getConfirmationUrl());

    datos.put("name_billing",      pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido());
    datos.put("email_billing",     pedido.getUsuario().getEmail());
    datos.put("mobilephone_billing", pedido.getUsuario().getCelular() != null ? pedido.getUsuario().getCelular() : "");
    datos.put("address_billing",   direccion != null ? direccion.getDireccion() : "");

    return datos;
}
}