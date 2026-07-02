package com.acmafer.modulos.pedidos.util;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;

class NumeroPedidoUtilTest {

    @Test
    @DisplayName("generar(): formato PED-YYYYMMDD-NNNN")
    void formato_correcto() {
        String n = NumeroPedidoUtil.generar();
        assertThat(n).matches("PED-\\d{8}-\\d{4}");
    }

    @Test
    @DisplayName("generar(): dos llamadas producen valores distintos")
    void valores_distintos() {
        assertThat(NumeroPedidoUtil.generar()).isNotEqualTo(NumeroPedidoUtil.generar());
    }

    @Test
    @DisplayName("generar(): formato tiene longitud esperada")
    void longitud_correcta() {
        String n = NumeroPedidoUtil.generar();
        assertThat(n).hasSize(17); // PED- + 8 + - + 4 = 17
    }
}
