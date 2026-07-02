package com.acmafer.modulos.pedidos.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {
    private List<ItemDTO> detalles;
    private String metodoPago;
    private String notas;

    private Long direccionEntregaId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDTO {
        private Long idProducto;
        private Integer cantidad;
    }
}