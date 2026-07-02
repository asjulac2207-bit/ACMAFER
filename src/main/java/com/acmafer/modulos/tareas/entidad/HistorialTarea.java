package com.acmafer.modulos.tareas.entidad;

import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_tarea")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialTarea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private Tarea.Estado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo")
    private Tarea.Estado nuevoEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    @Column(name = "fecha_cambio")
    @Builder.Default
    private LocalDateTime fechaCambio = LocalDateTime.now();
}
