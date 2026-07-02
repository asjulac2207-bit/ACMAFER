package com.acmafer.modulos.productos.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "Máximo 200 caracteres")
    private String nombre;

    private String descripcion;

    @Size(max = 50, message = "Máximo 50 caracteres")
    private String codigo;

    @NotNull(message = "Selecciona una categoría")
    private Long idCategoria;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precioUnitario;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockActual;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    private String estado;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String rutaImagen;
}