package com.acmafer.modulos.tareas.entidad;


import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "asignacion_tarea")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsignacionTarea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Usuario empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignado_por")
    private Usuario asignadoPor;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "comentario_admin", length = 1000)
    private String comentarioAdmin;

    @Column(name = "fecha_asignacion")
    @Builder.Default
    private LocalDateTime fechaAsignacion = LocalDateTime.now();
}
