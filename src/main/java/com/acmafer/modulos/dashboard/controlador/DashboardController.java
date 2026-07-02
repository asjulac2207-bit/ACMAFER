package com.acmafer.modulos.dashboard.controlador;

import com.acmafer.modulos.dashboard.dto.DashboardKpiDTO;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.productos.servicio.ProductoService;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final ProductoRepository productoRepo;
    private final PedidoRepository pedidoRepo;
    private final UsuarioRepository usuarioRepo;
    private final ProductoService productoService;

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    @GetMapping({ "", "/" })
    public String index(Model model, @AuthenticationPrincipal Usuario usuario) {
        model.addAttribute("titulo", "Dashboard");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("mostrarIntro", false);

        Rol rol = usuario.getRol();

        // ── ADMINISTRADOR ──────────────────────────────────────
        if (rol == Rol.ADMINISTRADOR) {
            DashboardKpiDTO kpis = DashboardKpiDTO.builder()
                    .totalUsuarios(usuarioRepo.count())
                    .usuariosActivos(usuarioRepo.countByEstado("Activo"))
                    .totalProductos(productoRepo.count())
                    .productosStockBajo((long) productoService.productosConStockBajo().size())
                    .pedidosPendientes(pedidoRepo.countByEstado("Pendiente"))
                    .pedidosProcesando(pedidoRepo.countByEstado("Procesando"))
                    .pedidosAprobados(pedidoRepo.countByEstado("Aprobado"))
                    .pedidosEntregados(pedidoRepo.countByEstado("Entregado"))
                    .pedidosCompletados(pedidoRepo.countByEstado("Completado"))
                    .tareasEnProgreso(0L) // reemplaza con tu TareaRepository si lo tienes
                    .tareasBloqueadas(0L) // reemplaza con tu TareaRepository si lo tienes
                    .tareasCompletadas(0L) // reemplaza con tu TareaRepository si lo tienes
                    .totalVentas(orZero(pedidoRepo.sumTotalVentas()))
                    .topProductos(productoService.topVendidos(6))
                    .stockBajo(productoService.productosConStockBajo())
                    .build();

            model.addAttribute("kpis", kpis);

            // Pedidos por mes para el line chart
            model.addAttribute("pedidosMes", buildMapaMes(pedidoRepo.pedidosPorMesAnioActual()));

            // Top 6 productos para gráfico de barras horizontales
            model.addAttribute("topProductos", productoService.topVendidos(6).stream()
                    .map(p -> Map.of(
                            "nombre", p.getNombre(),
                            "ventas", p.getVentasTotales()))
                    .toList());

            // Categorías para gráfico donut (si lo usas en admin)
            model.addAttribute("categoriasData", buildCategoriasData());

            return "dashboard/admin";
        }

        // ── SUPERVISOR ─────────────────────────────────────────
        if (rol == Rol.SUPERVISOR) {
            DashboardKpiDTO kpis = DashboardKpiDTO.builder()
                    .totalProductos(productoRepo.count())
                    .productosStockBajo((long) productoService.productosConStockBajo().size())
                    .pedidosPendientes(pedidoRepo.countByEstado("Pendiente"))
                    .pedidosProcesando(pedidoRepo.countByEstado("Procesando"))
                    .pedidosAprobados(pedidoRepo.countByEstado("Aprobado"))
                    .pedidosEntregados(pedidoRepo.countByEstado("Entregado"))
                    .pedidosCompletados(pedidoRepo.countByEstado("Completado"))
                    .tareasEnProgreso(0L) // TODO: Reemplaza con tu TareaRepository
                    .tareasBloqueadas(0L) // TODO: Reemplaza con tu TareaRepository
                    .totalVentas(orZero(pedidoRepo.sumTotalVentas()))
                    .topProductos(productoService.topVendidos(8))
                    .stockBajo(productoService.productosConStockBajo())
                    .build();

            model.addAttribute("kpis", kpis);

            model.addAttribute("pedidosMes", buildMapaMes(pedidoRepo.pedidosPorMesAnioActual()));

            model.addAttribute("topProductos", productoService.topVendidos(8).stream()
                    .map(p -> Map.of(
                            "nombre", p.getNombre(),
                            "ventas", p.getVentasTotales()))
                    .toList());

            return "dashboard/supervisor";
        }

        // ── VENDEDOR ───────────────────────────────────────────
        if (rol == Rol.VENDEDOR) {
            DashboardKpiDTO kpis = DashboardKpiDTO.builder()
                    .pedidosPendientes(pedidoRepo.countByEstado("Pendiente"))
                    .pedidosProcesando(pedidoRepo.countByEstado("Procesando"))
                    .pedidosEntregados(pedidoRepo.countByEstado("Entregado"))
                    .totalVentas(orZero(pedidoRepo.sumTotalVentas()))
                    .topProductos(productoService.topVendidos(6))
                    .build();
            model.addAttribute("kpis", kpis);

            // Últimos 10 pedidos
            model.addAttribute("ultimosPedidos",
                    pedidoRepo.findAllByOrderByFechaPedidoDesc(PageRequest.of(0, 10)).getContent());

            // Datos mensuales para gráficas de área
            model.addAttribute("ventasMensuales", buildSeries(pedidoRepo.ventasPorMesAnioActual()));
            model.addAttribute("pedidosMensuales", buildSeries(pedidoRepo.pedidosPorMesAnioActual()));
            model.addAttribute("mesesLabels", buildLabels());

            return "dashboard/vendedor";
        }

        // ── TRABAJADOR ─────────────────────────────────────────
        if (rol == Rol.TRABAJADOR) {
            DashboardKpiDTO kpis = DashboardKpiDTO.builder()
                    .misPedidos((long) pedidoRepo
                            .findByUsuarioIdOrderByFechaPedidoDesc(usuario.getId()).size())
                    .build();
            model.addAttribute("kpis", kpis);
            return "dashboard/trabajador";
        }

        // ── CLIENTE (default) ──────────────────────────────────
        DashboardKpiDTO kpis = DashboardKpiDTO.builder()
                .misPedidos((long) pedidoRepo
                        .findByUsuarioIdOrderByFechaPedidoDesc(usuario.getId()).size())
                .build();
        model.addAttribute("kpis", kpis);
        return "dashboard/cliente";
    }

    // ── Helpers ────────────────────────────────────────────────────

    /**
     * Convierte result de query (mes 1-12, valor) a Map<Integer, Long>
     * para uso directo en Thymeleaf/JS del dashboard admin.
     */
    private Map<Integer, Long> buildMapaMes(List<Object[]> rows) {
        Map<Integer, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int mes = ((Number) row[0]).intValue();
            long cant = ((Number) row[1]).longValue();
            map.put(mes, cant);
        }
        return map;
    }

    /**
     * Convierte result de query (mes, valor) a array de 12 posiciones.
     * Usado por vendedor para gráficas de área.
     */
    private BigDecimal[] buildSeries(List<Object[]> rows) {
        BigDecimal[] arr = new BigDecimal[12];
        for (int i = 0; i < 12; i++)
            arr[i] = BigDecimal.ZERO;
        for (Object[] row : rows) {
            int mes = ((Number) row[0]).intValue() - 1; // 0-based
            arr[mes] = new BigDecimal(row[1].toString());
        }
        return arr;
    }

    /** Etiquetas de los 12 meses. */
    private String[] buildLabels() {
        return MESES;
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private Map<String, Long> buildCategoriasData() {
        Map<String, Long> map = new LinkedHashMap<>();
        productoRepo.findAll().forEach(p -> {
            String cat = p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría";
            map.merge(cat, 1L, Long::sum);
        });
        return map;
    }
}