package com.acmafer.modulos.usuarios.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "documento", length = 20, unique = true)
    private String documento;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre")
    private String nombre;

    @NotBlank
    @Size(max = 100)
    @Column(name = "apellido")
    private String apellido;

    @Email
    @NotBlank
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "celular", length = 15)
    private String celular;

    @Column(name = "genero", length = 1)
    private String genero; // "M", "F", "O"

    @Column(name = "proveedor", length = 20)
    @Builder.Default
    private String proveedor = "LOCAL";

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "usar_foto_google")
    @Builder.Default
    private Boolean usarFotoGoogle = true;

    @Column(name = "clave", length = 255)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_rol", length = 20)
    private Rol rol;

    @Column(name = "estado", length = 20)
    @Builder.Default
    private String estado = "Activo";

    @Column(name = "intentos_fallidos")
    @Builder.Default
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado")
    @Builder.Default
    private Boolean bloqueado = false;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "fecha_registro")
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(bloqueado);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "Activo".equals(estado);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getIniciales() {
        String n = nombre != null && !nombre.isEmpty() ? nombre.substring(0, 1).toUpperCase() : "";
        String a = apellido != null && !apellido.isEmpty() ? apellido.substring(0, 1).toUpperCase() : "";
        return n + a;
    }
}
