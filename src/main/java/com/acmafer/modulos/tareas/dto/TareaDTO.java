package com.acmafer.modulos.tareas.dto;


import com.acmafer.modulos.tareas.entidad.Tarea;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TareaDTO {
    @NotBlank(message = "El título es obligatorio")
    private String titulo;
    private String descripcion;
    private Tarea.Prioridad prioridad;
    private LocalDate fechaVencimiento;
    private Long idEmpleado;
    private String comentarioAdmin;
    private Long idPedidoRelacionado;
}
