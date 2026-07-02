package com.acmafer.modulos.usuarios.entidad;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "token_recuperacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenRecuperacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(name = "token", unique = true, length = 100) private String token;
    @Column(name = "fecha_expiracion") private LocalDateTime fechaExpiracion;
    @Column(name = "usado") @Builder.Default private Boolean usado = false;

    public boolean estaVigente() {
        return !Boolean.TRUE.equals(usado) && LocalDateTime.now().isBefore(fechaExpiracion);
    }
}
