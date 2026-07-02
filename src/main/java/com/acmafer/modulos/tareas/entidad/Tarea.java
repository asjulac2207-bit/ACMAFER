package com.acmafer.modulos.tareas.entidad;

import com.acmafer.modulos.pedidos.entidad.Pedido;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    public enum Prioridad {
        ALTA, MEDIA, BAJA
    }

    public enum Estado {
        NO_INICIADA, EN_PROGRESO, BLOQUEADA, COMPLETADA, CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad")
    @Builder.Default
    private Prioridad prioridad = Prioridad.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private Estado estado = Estado.NO_INICIADA;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido_relacionado")
    private Pedido pedidoRelacionado;

    @Column(name = "ruta_evidencia", length = 500)
    private String rutaEvidencia;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}