package com.acmafer.modulos.pedidos.servicio;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.servicio.EmailService;
import com.acmafer.modulos.notificaciones.entidad.Notificacion;
import com.acmafer.modulos.notificaciones.repositorio.NotificacionRepository;
import com.acmafer.modulos.pedidos.dto.PedidoDTO;
import com.acmafer.modulos.pedidos.entidad.DetallePedido;
import com.acmafer.modulos.pedidos.entidad.DireccionEntrega;
import com.acmafer.modulos.pedidos.entidad.Pedido;
import com.acmafer.modulos.pedidos.repositorio.DireccionEntregaRepository;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepo;
    private final DireccionEntregaRepository direccionRepo;
    private final NotificacionRepository notificacionRepo;

    public static final String PENDIENTE = "Pendiente";
    public static final String PROCESANDO = "Procesando";
    public static final String ENVIADO = "Enviado";
    public static final String ENTREGADO = "Entregado";
    public static final String CANCELADO = "Cancelado";
    public static final String PENDIENTE_PAGO = "Pendiente pago";

    @Transactional
    public Pedido crear(PedidoDTO dto, Usuario solicitante) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty())
            throw new BusinessException("El pedido debe tener al menos un producto");

        // Generar número de pedido
        String numPedido = generarNumeroPedido();
        BigDecimal total = BigDecimal.ZERO;

        // Buscar y validar la dirección de entrega
        DireccionEntrega direccion = null;
        if (dto.getDireccionEntregaId() != null) {
            direccion = direccionRepo.findById(dto.getDireccionEntregaId())
                    .orElseThrow(() -> new BusinessException("Dirección de entrega no encontrada"));
            // Verificar que la dirección pertenezca al usuario
            if (!direccion.getUsuario().getId().equals(solicitante.getId())) {
                throw new BusinessException("La dirección no pertenece al usuario");
            }
        }

        // Construir el pedido
        Pedido pedido = Pedido.builder()
                .numeroPedido(numPedido)
                .usuario(solicitante)
                .estado(PENDIENTE)
                .metodoPago(dto.getMetodoPago())
                .notas(dto.getNotas())
                .direccionEntrega(direccion)
                .build();

        // Procesar los detalles del pedido
        List<DetallePedido> detalles = new ArrayList<>();
        for (var item : dto.getDetalles()) {
            Producto p = productoRepo.findById(item.getIdProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado: " + item.getIdProducto()));
            if (p.getStockActual() < item.getCantidad())
                throw new BusinessException("Stock insuficiente para: " + p.getNombre());

            BigDecimal sub = p.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(sub);

            detalles.add(DetallePedido.builder()
                    .pedido(pedido).producto(p)
                    .cantidad(item.getCantidad())
                    .precioUnitario(p.getPrecioUnitario())
                    .subtotal(sub).build());

            // Actualizar stock del producto
            p.setStockActual(p.getStockActual() - item.getCantidad());
            if (p.getStockActual() == 0)
                p.setEstado("Agotado");
            productoRepo.save(p);
        }

        pedido.setTotal(total);
        pedido.setDetalles(detalles);
        Pedido saved = pedidoRepo.save(pedido);
        log.info("Pedido {} creado por {}", saved.getNumeroPedido(), solicitante.getEmail());

        // ── Notificar a vendedores sobre el nuevo pedido ──────────────
        notificarVendedoresPedidoNuevo(saved, solicitante);

        return saved;
    }

    @Transactional
    public Pedido cambiarEstado(Long idPedido, String nuevoEstado, String observacion, Usuario responsable) {
        Pedido pedido = buscarPorId(idPedido);
        String anterior = pedido.getEstado();

        // Si el estado es el mismo, solo actualizar observación si existe
        if (anterior.equalsIgnoreCase(nuevoEstado)) {
            if (observacion != null && !observacion.isBlank()) {
                pedido.setNotas(observacion);
                pedidoRepo.save(pedido);
                log.info("Pedido {} - Se actualizó observación pero no cambió el estado", pedido.getNumeroPedido());
            }
            return pedido;
        }

        validarTransicion(anterior, nuevoEstado);

        // Si es entregado, aumentar ventas totales
        if (ENTREGADO.equals(nuevoEstado)) {
            pedido.getDetalles().forEach(d -> {
                Producto p = d.getProducto();
                if (p != null) {
                    p.setVentasTotales(p.getVentasTotales() + d.getCantidad());
                    if (!"Descontinuado".equals(p.getEstado()))
                        p.setEstado("Disponible");
                    productoRepo.save(p);
                }
            });
        }
        // Si se cancela después de procesar, restaurar stock
        if (CANCELADO.equals(nuevoEstado) && PROCESANDO.equals(anterior)) {
            pedido.getDetalles().forEach(d -> {
                Producto p = d.getProducto();
                if (p != null) {
                    p.setStockActual(p.getStockActual() + d.getCantidad());
                    if (p.getStockActual() > 0 && !"Descontinuado".equals(p.getEstado()))
                        p.setEstado("Disponible");
                    productoRepo.save(p);
                }
            });
        }

        pedido.setEstado(nuevoEstado);
        if (observacion != null && !observacion.isBlank())
            pedido.setNotas(observacion);
        Pedido saved = pedidoRepo.save(pedido);
        log.info("Pedido {} {} → {} por {}", saved.getNumeroPedido(), anterior, nuevoEstado, responsable.getEmail());

        // ── EXTRAER DATOS ANTES DE ENVIAR CORREOS ─────────────────
        String emailCliente = null;
        String nombreCliente = "N/A";
        if (saved.getUsuario() != null) {
            emailCliente = saved.getUsuario().getEmail();
            nombreCliente = saved.getUsuario().getNombreCompleto();
        }

        String numeroPedido = saved.getNumeroPedido();
        String nombreResponsable = responsable.getNombreCompleto();

        // ── ENVIAR CORREOS FUERA DE LA TRANSACCIÓN (con try-catch) ──
        try {
            // 1. Notificar al cliente
            if (emailCliente != null) {
                emailService.notificarCambioEstadoPedido(
                        emailCliente,
                        numeroPedido,
                        anterior,
                        nuevoEstado);
            }

            // 2. Notificar a administradores
            List<Usuario> admins = usuarioRepo.findActivosByRol(Rol.ADMINISTRADOR);
            for (Usuario admin : admins) {
                emailService.enviarNotificacion(
                        admin.getEmail(),
                        "📦 Pedido " + numeroPedido + " → " + nuevoEstado,
                        "El pedido <strong>" + numeroPedido + "</strong> del cliente <strong>" + nombreCliente +
                                "</strong> cambió de <em>" + anterior + "</em> a <strong>" + nuevoEstado
                                + "</strong>.<br/>" +
                                "Actualizado por: " + nombreResponsable);
            }
        } catch (Exception e) {
            log.error("Error al enviar correos de cambio de estado para pedido {}: {}",
                    numeroPedido, e.getMessage(), e);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Pedido> listarTodos(String estado, Pageable pageable) {
        return pedidoRepo.filtrar(estado, pageable);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuario(Long idUsuario) {
        return pedidoRepo.findByUsuarioIdOrderByFechaPedidoDesc(idUsuario);
    }

    private void validarTransicion(String actual, String nuevo) {
        if (ENTREGADO.equals(actual))
            throw new BusinessException("No se puede modificar un pedido ya entregado");
        if (CANCELADO.equals(actual))
            throw new BusinessException("No se puede modificar un pedido cancelado");
    }

    private String generarNumeroPedido() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ACM-" + ts + "-" + String.format("%04d", (int) (Math.random() * 9999));
    }

    @Transactional
    public Pedido crearConEstado(PedidoDTO dto, Usuario solicitante, String estadoInicial) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty())
            throw new BusinessException("El pedido debe tener al menos un producto");

        String numPedido = generarNumeroPedido();
        BigDecimal total = BigDecimal.ZERO;

        DireccionEntrega direccion = null;
        if (dto.getDireccionEntregaId() != null) {
            direccion = direccionRepo.findById(dto.getDireccionEntregaId())
                    .orElseThrow(() -> new BusinessException("Dirección no encontrada"));
            if (!direccion.getUsuario().getId().equals(solicitante.getId()))
                throw new BusinessException("La dirección no pertenece al usuario");
        }

        Pedido pedido = Pedido.builder()
                .numeroPedido(numPedido)
                .usuario(solicitante)
                .estado(estadoInicial)
                .metodoPago(dto.getMetodoPago())
                .notas(dto.getNotas())
                .direccionEntrega(direccion)
                .build();

        List<DetallePedido> detalles = new ArrayList<>();
        for (var item : dto.getDetalles()) {
            Producto p = productoRepo.findById(item.getIdProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado: " + item.getIdProducto()));
            if (p.getStockActual() < item.getCantidad())
                throw new BusinessException("Stock insuficiente para: " + p.getNombre());

            BigDecimal sub = p.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(sub);

            detalles.add(DetallePedido.builder()
                    .pedido(pedido).producto(p)
                    .cantidad(item.getCantidad())
                    .precioUnitario(p.getPrecioUnitario())
                    .subtotal(sub).build());

            p.setStockActual(p.getStockActual() - item.getCantidad());
            if (p.getStockActual() == 0) p.setEstado("Agotado");
            productoRepo.save(p);
        }

        pedido.setTotal(total);
        pedido.setDetalles(detalles);
        Pedido saved = pedidoRepo.save(pedido);
        log.info("Pedido {} creado con estado '{}' por {}", saved.getNumeroPedido(), estadoInicial, solicitante.getEmail());

        // ── Notificar a vendedores sobre el nuevo pedido ──────────────
        notificarVendedoresPedidoNuevo(saved, solicitante);

        return saved;
    }

    @Transactional
    public void eliminar(Long idPedido, Usuario solicitante) {
        Pedido pedido = buscarPorId(idPedido);

        if (!pedido.getUsuario().getId().equals(solicitante.getId())) {
            throw new BusinessException("No tienes permiso para eliminar este pedido");
        }
        if (!PENDIENTE.equals(pedido.getEstado())) {
            throw new BusinessException(
                "Solo puedes eliminar pedidos en estado Pendiente. " +
                "Este pedido está en estado: " + pedido.getEstado());
        }

        // Restaurar stock
        pedido.getDetalles().forEach(d -> {
            Producto p = d.getProducto();
            if (p != null) {
                p.setStockActual(p.getStockActual() + d.getCantidad());
                if (p.getStockActual() > 0 && !"Descontinuado".equals(p.getEstado()))
                    p.setEstado("Disponible");
                productoRepo.save(p);
            }
        });

        pedidoRepo.delete(pedido);
        log.info("Pedido {} eliminado por {}", pedido.getNumeroPedido(), solicitante.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuarioConFiltro(Long idUsuario, String estado) {
        String filtro = (estado != null && !estado.isBlank()) ? estado : null;
        return pedidoRepo.findByUsuarioIdConFiltro(idUsuario, filtro);
    }

    // ═══════════════════════════════════════════════════════════════
    // PRIVADO — Notifica a todos los vendedores activos cuando entra
    // un pedido nuevo, para que puedan confirmarlo desde su panel.
    // Se ejecuta en try/catch para no afectar la creación del pedido
    // si llegara a fallar el guardado de la notificación.
    // ═══════════════════════════════════════════════════════════════
    private void notificarVendedoresPedidoNuevo(Pedido pedido, Usuario solicitante) {
        try {
            List<Usuario> vendedores = usuarioRepo.findActivosByRol(Rol.VENDEDOR);
            for (Usuario v : vendedores) {
                Notificacion n = Notificacion.builder()
                        .destinatario(v)
                        .mensaje("Nuevo pedido " + pedido.getNumeroPedido() + " de "
                                + solicitante.getNombreCompleto())
                        .tipo("pedido_nuevo")
                        .urlDestino("/pedidos/" + pedido.getId())
                        .build();
                notificacionRepo.save(n);
            }
        } catch (Exception e) {
            log.error("Error al crear notificaciones de pedido nuevo para pedido {}: {}",
                    pedido.getNumeroPedido(), e.getMessage(), e);
        }
    }
}