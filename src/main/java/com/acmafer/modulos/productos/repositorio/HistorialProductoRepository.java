package com.acmafer.modulos.productos.repositorio;


import com.acmafer.modulos.productos.entidad.HistorialProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialProductoRepository extends JpaRepository<HistorialProducto, Long> {
    List<HistorialProducto> findByProductoIdOrderByFechaDesc(Long productoId);
}
