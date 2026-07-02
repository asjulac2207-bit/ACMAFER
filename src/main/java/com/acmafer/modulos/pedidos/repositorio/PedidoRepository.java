package com.acmafer.modulos.pedidos.repositorio;

import com.acmafer.modulos.pedidos.entidad.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId);

    Page<Pedido> findAllByOrderByFechaPedidoDesc(Pageable pageable);

    long countByEstado(String estado);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado != 'Cancelado'")
    java.math.BigDecimal sumTotalVentas();

    @Query(value = "SELECT p FROM Pedido p WHERE (:estado IS NULL OR p.estado = :estado)",
           countQuery = "SELECT COUNT(p) FROM Pedido p WHERE (:estado IS NULL OR p.estado = :estado)")
    Page<Pedido> filtrar(@Param("estado") String estado, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId " +
           "AND (:estado IS NULL OR :estado = '' OR p.estado = :estado) " +
           "ORDER BY p.fechaPedido DESC")
    List<Pedido> findByUsuarioIdConFiltro(@Param("usuarioId") Long usuarioId,
                                          @Param("estado") String estado);

    /**
     * Ventas totales agrupadas por mes para el año actual.
     * Retorna lista de Object[]: [mes(1-12), totalVentas]
     */
    @Query("SELECT MONTH(p.fechaPedido), COALESCE(SUM(p.total), 0) " +
           "FROM Pedido p " +
           "WHERE p.estado != 'Cancelado' " +
           "AND YEAR(p.fechaPedido) = YEAR(CURRENT_DATE) " +
           "GROUP BY MONTH(p.fechaPedido) " +
           "ORDER BY MONTH(p.fechaPedido)")
    List<Object[]> ventasPorMesAnioActual();

    /**
     * Cantidad de pedidos por mes para el año actual.
     * Retorna lista de Object[]: [mes(1-12), cantidad]
     */
    @Query("SELECT MONTH(p.fechaPedido), COUNT(p) " +
           "FROM Pedido p " +
           "WHERE YEAR(p.fechaPedido) = YEAR(CURRENT_DATE) " +
           "GROUP BY MONTH(p.fechaPedido) " +
           "ORDER BY MONTH(p.fechaPedido)")
    List<Object[]> pedidosPorMesAnioActual();
}