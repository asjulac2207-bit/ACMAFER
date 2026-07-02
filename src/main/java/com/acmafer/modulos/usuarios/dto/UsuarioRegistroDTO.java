package com.acmafer.modulos.usuarios.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRegistroDTO {
    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 20)
    private String documento;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String apellido;
    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;
    @Size(max = 15)
    private String celular;

    // Validación condicional: solo valida si la contraseña NO está vacía
    @Pattern(regexp = "^$|.{8,}", message = "Mínimo 8 caracteres")
    private String clave;
}