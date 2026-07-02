package com.acmafer.modulos.pedidos.repositorio;

import com.acmafer.modulos.pedidos.entidad.HistorialPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialPedidoRepository extends JpaRepository<HistorialPedido, Long> {
    List<HistorialPedido> findByPedidoIdOrderByFechaCambioDesc(Long idPedido);
}
