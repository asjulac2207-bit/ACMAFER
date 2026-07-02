package com.acmafer.modulos.pedidos.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class NumeroPedidoUtil {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger SEQ = new AtomicInteger((int)(Math.random()*1000));

    public static String generar() {
        int seq = SEQ.incrementAndGet() % 10000;
        return "PED-" + LocalDateTime.now().format(FMT) + "-" + String.format("%04d", seq);
    }
    private NumeroPedidoUtil() {}
}
