package com.acmafer.modulos.productos.servicio;


import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.modulos.productos.dto.ProductoDTO;
import com.acmafer.modulos.productos.entidad.Categoria;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.CategoriaRepository;
import com.acmafer.modulos.productos.repositorio.HistorialProductoRepository;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepo;
    @Mock private CategoriaRepository categoriaRepo;
    @Mock private HistorialProductoRepository historialRepo;
    @InjectMocks private ProductoService productoService;

    private Usuario admin;
    private Categoria cat;
    private Producto producto;

    @BeforeEach
    void setUp() {
        admin = Usuario.builder()
            .id(1L).nombre("Admin").apellido("Test")
            .email("admin@acmafer.com").rol(Rol.ADMINISTRADOR).build();
        cat = Categoria.builder().id(1L).nombre("Fundición").activo(true).build();
        producto = Producto.builder()
            .id(1L).nombre("Engranaje Test").codigo("ENG-001")
            .precioUnitario(new BigDecimal("500000"))
            .stockActual(100).stockMinimo(10)
            .estado("Disponible").categoria(cat).build();
    }

    @Test
    @DisplayName("crear(): guarda el producto y lo retorna con ID")
    void crear_productoValido() {
        ProductoDTO dto = ProductoDTO.builder()
            .nombre("Engranaje Test").codigo("ENG-001")
            .idCategoria(1L).precioUnitario(new BigDecimal("500000"))
            .stockActual(100).stockMinimo(10).build();

        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat));
        when(productoRepo.findByCodigo("ENG-001")).thenReturn(Optional.empty());
        when(productoRepo.save(any())).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Producto resultado = productoService.crear(dto, admin);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Engranaje Test");
        verify(productoRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("crear(): código duplicado lanza BusinessException")
    void crear_codigoDuplicado() {
        ProductoDTO dto = ProductoDTO.builder()
            .nombre("Otro").codigo("ENG-001")
            .idCategoria(1L).precioUnitario(BigDecimal.TEN)
            .stockActual(1).build();

        when(productoRepo.findByCodigo("ENG-001")).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> productoService.crear(dto, admin))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("código");
    }

    @Test
    @DisplayName("cambiarEstado(): no descontinuar con stock > 0")
    void cambiarEstado_descontinuarConStock() {
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        assertThatThrownBy(() -> productoService.cambiarEstado(1L, "Descontinuado", admin))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("cambiarEstado(): stock 0 permite descontinuar")
    void cambiarEstado_sinStock_descontinua() {
        producto.setStockActual(0);
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historialRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(
            () -> productoService.cambiarEstado(1L, "Descontinuado", admin));
        assertThat(producto.getEstado()).isEqualTo("Descontinuado");
    }

    @Test
    @DisplayName("buscarPorId(): lanza excepción si no existe")
    void buscarPorId_noExiste() {
        when(productoRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productoService.buscarPorId(99L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("topVendidos(): delega correctamente al repositorio")
    void topVendidos_llama_repositorio() {
        when(productoRepo.findTopVendidos(any())).thenReturn(List.of(producto));
        var result = productoService.topVendidos(5);
        assertThat(result).hasSize(1).first().extracting(Producto::getCodigo).isEqualTo("ENG-001");
    }
}
