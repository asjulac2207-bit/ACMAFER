package com.acmafer.modulos.tareas.entidad;


import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentario_tarea")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComentarioTarea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    @Column(name = "contenido", length = 2000)
    private String contenido;

    @Column(name = "fecha_comentario")
    @Builder.Default
    private LocalDateTime fechaComentario = LocalDateTime.now();
}
