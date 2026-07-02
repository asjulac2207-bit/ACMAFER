package com.acmafer.modulos.pedidos.entidad;


import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialPedido {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 30)
    private String estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario modificadoPor;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "fecha_cambio")
    @Builder.Default
    private LocalDateTime fechaCambio = LocalDateTime.now();
}
