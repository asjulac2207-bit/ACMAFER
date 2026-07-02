package com.acmafer.modulos.usuarios.servicio;


import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.servicio.EmailService;
import com.acmafer.modulos.usuarios.dto.UsuarioRegistroDTO;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepo;
    @Mock private EmailService emailService;
    @Spy  private PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    @InjectMocks private UsuarioService usuarioService;

    private UsuarioRegistroDTO dto;

    @BeforeEach
    void setUp() {
        dto = UsuarioRegistroDTO.builder()
            .documento("12345678")
            .nombre("Test").apellido("User")
            .email("test@acmafer.com")
            .clave("Password123").build();
    }

    @Test
    @DisplayName("registrar(): crea usuario con rol CLIENTE")
    void registrar_clientePorDefecto() {
        when(usuarioRepo.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepo.existsByDocumento(anyString())).thenReturn(false);
        when(usuarioRepo.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        Usuario result = usuarioService.registrar(dto);

        assertThat(result.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(result.getEstado()).isEqualTo("Activo");
        verify(emailService).enviarBienvenida(any(Usuario.class));
    }

    @Test
    @DisplayName("registrar(): email duplicado lanza BusinessException")
    void registrar_emailDuplicado() {
        when(usuarioRepo.existsByEmail(anyString())).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.registrar(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("registrar(): documento duplicado lanza BusinessException")
    void registrar_documentoDuplicado() {
        when(usuarioRepo.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepo.existsByDocumento(anyString())).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.registrar(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("documento");
    }

    @Test
    @DisplayName("registrarIntentoFallido(): bloquea tras 5 intentos")
    void registrarIntento_bloqueo() {
        Usuario u = Usuario.builder().id(1L).email("test@acmafer.com")
            .intentosFallidos(4).bloqueado(false).build();
        when(usuarioRepo.findByEmail("test@acmafer.com")).thenReturn(Optional.of(u));
        when(usuarioRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.registrarIntentoFallido("test@acmafer.com");

        assertThat(u.getBloqueado()).isTrue();
        assertThat(u.getIntentosFallidos()).isEqualTo(5);
        verify(emailService).notificarCuentaBloqueada(u);
    }

    @Test
    @DisplayName("cambiarEstado(): desbloquea y resetea intentos")
    void cambiarEstado_activar_resetea() {
        Usuario u = Usuario.builder().id(1L).estado("Inactivo")
            .bloqueado(true).intentosFallidos(5).build();
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.cambiarEstado(1L, "Activo");

        assertThat(u.getBloqueado()).isFalse();
        assertThat(u.getIntentosFallidos()).isEqualTo(0);
    }
}
