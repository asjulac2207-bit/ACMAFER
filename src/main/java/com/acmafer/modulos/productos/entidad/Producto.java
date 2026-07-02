package com.acmafer.modulos.productos.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "codigo", unique = true, length = 50)
    private String codigo;

    @Min(0)
    @Column(name = "stock_actual")
    @Builder.Default
    private Integer stockActual = 0;

    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 5;

    @Column(name = "estado", length = 30)
    @Builder.Default
    private String estado = "Disponible";

    @Column(name = "ruta_imagen", length = 500)
    private String rutaImagen;

    @Column(name = "precio_unitario", precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ventas acumuladas para ranking "más vendidos"
    @Column(name = "ventas_totales")
    @Builder.Default
    private Integer ventasTotales = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    public boolean estaDisponible() {
        return "Disponible".equals(estado) && stockActual != null && stockActual > 0;
    }

    public boolean tieneStockBajo() {
        if (stockActual == null || stockMinimo == null)
            return false;
        return stockActual <= stockMinimo && stockActual > 0;
    }
}
