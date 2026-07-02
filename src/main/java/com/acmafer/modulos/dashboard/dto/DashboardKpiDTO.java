package com.acmafer.modulos.dashboard.dto;

import com.acmafer.modulos.productos.entidad.Producto;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardKpiDTO {

    // ── Usuarios (admin) ──────────────────────────────
    private Long totalUsuarios;
    private Long usuariosActivos;

    // ── Productos ─────────────────────────────────────
    private Long totalProductos;
    private Long productosDisponibles;
    private Long productosStockBajo;

    // ── Pedidos (admin/supervisor/vendedor) ───────────
    private Long totalPedidos;
    private Long pedidosPendientes;
    private Long pedidosProcesando;
    private Long pedidosAprobados;
    private Long pedidosEntregados;
    private Long pedidosCompletados;

    // ── Ventas ────────────────────────────────────────
    private BigDecimal totalVentas;

    // ── Tareas (admin/supervisor/trabajador) ──────────
    private Long tareasEnProgreso;
    private Long tareasBloqueadas;
    private Long tareasCompletadas;
    private Long misTareasActivas;
    private Long misTareasCompletadas;

    // ── Personales (cliente/trabajador) ──────────────
    private Long misPedidos;

    // ── Listas ────────────────────────────────────────
    private List<Producto> topProductos;
    private List<Producto> stockBajo;
}