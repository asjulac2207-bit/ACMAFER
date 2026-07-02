package com.acmafer.modulos.productos.entidad;


import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "historial_producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialProducto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "campo_modificado", length = 50)
    private String campoModificado;

    @Column(name = "valor_anterior", length = 500)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 500)
    private String valorNuevo;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_usuario")
    private Usuario modificadoPor;

    @Column(name = "fecha")
    @Builder.Default private LocalDateTime fecha = LocalDateTime.now();
}
