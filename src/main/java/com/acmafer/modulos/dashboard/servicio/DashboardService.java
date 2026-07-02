package com.acmafer.modulos.dashboard.servicio;


import com.acmafer.modulos.dashboard.dto.DashboardKpiDTO;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.tareas.repositorio.TareaRepository;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepo;
    private final PedidoRepository pedidoRepo;
    private final UsuarioRepository usuarioRepo;
    private final TareaRepository tareaRepo;

    public DashboardKpiDTO getKpis() {
        var ventas = pedidoRepo.sumTotalVentas();
        return DashboardKpiDTO.builder()
            .totalProductos(productoRepo.count())
            .productosDisponibles(productoRepo.countByEstado("Disponible"))
            .productosStockBajo((long) productoRepo.findConStockBajo().size())
            .totalPedidos(pedidoRepo.count())
            .pedidosPendientes(pedidoRepo.countByEstado("Pendiente"))
            .pedidosProcesando(pedidoRepo.countByEstado("Procesando"))
            .pedidosEntregados(pedidoRepo.countByEstado("Entregado"))
            .totalVentas(ventas != null ? ventas : java.math.BigDecimal.ZERO)
            .totalUsuarios(usuarioRepo.count())
            .topProductos(productoRepo.findTopVendidos(PageRequest.of(0, 5)))
            .stockBajo(productoRepo.findConStockBajo())
            .build();
    }
}
