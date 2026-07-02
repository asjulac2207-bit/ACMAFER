package com.acmafer.modulos.notificaciones.entidad;


import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario destinatario;

    @Column(name = "mensaje", length = 500)
    private String mensaje;

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "leida")
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "url_destino", length = 200)
    private String urlDestino;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
