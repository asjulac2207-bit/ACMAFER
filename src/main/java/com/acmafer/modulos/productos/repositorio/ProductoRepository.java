package com.acmafer.modulos.productos.repositorio;


import com.acmafer.modulos.productos.entidad.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    Page<Producto> findByEstado(String estado, Pageable pageable);

    @Query(value = "SELECT * FROM producto p WHERE " +
               "(:q IS NULL OR LOWER(CAST(p.nombre AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%')) " +
               "OR LOWER(CAST(p.codigo AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))) " +
               "AND (:estado IS NULL OR CAST(p.estado AS TEXT) = CAST(:estado AS TEXT)) " +
               "AND (:idCat IS NULL OR p.id_categoria = :idCat) " +
               "ORDER BY p.nombre", 
       countQuery = "SELECT COUNT(*) FROM producto p WHERE " +
                    "(:q IS NULL OR LOWER(CAST(p.nombre AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%')) " +
                    "OR LOWER(CAST(p.codigo AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))) " +
                    "AND (:estado IS NULL OR CAST(p.estado AS TEXT) = CAST(:estado AS TEXT)) " +
                    "AND (:idCat IS NULL OR p.id_categoria = :idCat)",
       nativeQuery = true)
Page<Producto> filtrar(@Param("q") String q,
                       @Param("estado") String estado,
                       @Param("idCat") Long idCat,
                       Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.estado = 'Disponible' ORDER BY p.ventasTotales DESC")
    List<Producto> findTopVendidos(Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.estado = 'Disponible'")
    List<Producto> findConStockBajo();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.estado = 'Disponible'")
long countConStockBajo();

    List<Producto> findByEstadoOrderByVentasTotalesDesc(String estado);

    long countByEstado(String estado);
    
     @Query("SELECT p FROM Producto p WHERE p.estado = 'Disponible' ORDER BY p.ventasTotales DESC")
        List<Producto> findTop6MasVendidos(Pageable pageable);
}
