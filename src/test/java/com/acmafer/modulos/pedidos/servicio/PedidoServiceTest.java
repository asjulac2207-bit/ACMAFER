package com.acmafer.modulos.pedidos.servicio;


import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.servicio.EmailService;
import com.acmafer.modulos.pedidos.dto.PedidoDTO;
import com.acmafer.modulos.pedidos.entidad.Pedido;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepo;
    @Mock private ProductoRepository productoRepo;
    @Mock private EmailService emailService;
    @InjectMocks private PedidoService pedidoService;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
            .id(1L).nombre("Test").apellido("User")
            .email("test@acmafer.com").rol(Rol.CLIENTE).build();
        producto = Producto.builder()
            .id(1L).nombre("Engranaje Test")
            .codigo("TEST-001")
            .precioUnitario(new BigDecimal("100000"))
            .stockActual(50)
            .estado("Disponible").build();
    }

    @Test
    @DisplayName("Crear pedido con detalles válidos")
    void crearPedidoValido() {
        PedidoDTO.ItemDTO item = new PedidoDTO.ItemDTO(1L, 2);
        PedidoDTO dto = PedidoDTO.builder()
            .detalles(List.of(item))
            .metodoPago("PayCon")
            .build();

        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepo.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido result = pedidoService.crear(dto, usuario);

        assertNotNull(result);
        assertNotNull(result.getNumeroPedido());
        assertEquals("Pendiente", result.getEstado());
        assertEquals(new BigDecimal("200000"), result.getTotal());
        verify(productoRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("Crear pedido sin detalles lanza excepción")
    void crearPedidoSinDetalles() {
        PedidoDTO dto = PedidoDTO.builder().detalles(Collections.emptyList()).build();
        assertThrows(BusinessException.class, () -> pedidoService.crear(dto, usuario));
    }

    @Test
    @DisplayName("Crear pedido con stock insuficiente lanza excepción")
    void crearPedidoStockInsuficiente() {
        producto.setStockActual(1);
        PedidoDTO.ItemDTO item = new PedidoDTO.ItemDTO(1L, 5);
        PedidoDTO dto = PedidoDTO.builder().detalles(List.of(item)).build();
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        assertThrows(BusinessException.class, () -> pedidoService.crear(dto, usuario));
    }

    @Test
    @DisplayName("Cambiar estado de Pendiente a Procesando")
    void cambiarEstadoValido() {
        Pedido pedido = Pedido.builder()
            .id(1L).numeroPedido("ACM-001")
            .estado("Pendiente")
            .detalles(new ArrayList<>()).build();
        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pedido result = pedidoService.cambiarEstado(1L, "Procesando", "Aprobado", usuario);
        assertEquals("Procesando", result.getEstado());
    }

    @Test
    @DisplayName("No se puede modificar pedido Entregado")
    void noPuedeModificarEntregado() {
        Pedido pedido = Pedido.builder()
            .id(1L).estado("Entregado").detalles(new ArrayList<>()).build();
        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        assertThrows(BusinessException.class,
            () -> pedidoService.cambiarEstado(1L, "Cancelado", "", usuario));
    }

    @Test
    @DisplayName("Buscar pedido inexistente lanza excepción")
    void buscarPedidoInexistente() {
        when(pedidoRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> pedidoService.buscarPorId(99L));
    }
}
