package com.acmafer.modulos.pedidos.controlador;

import com.acmafer.comun.configuracion.EpaycoConfig;
import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.comun.seguridad.CustomOAuth2User;
import com.acmafer.modulos.pedidos.dto.PedidoDTO;
import com.acmafer.modulos.pedidos.entidad.DireccionEntrega;
import com.acmafer.modulos.pedidos.entidad.Pedido;
import com.acmafer.modulos.pedidos.servicio.DireccionEntregaService;
import com.acmafer.modulos.pedidos.servicio.PagoService;
import com.acmafer.modulos.pedidos.servicio.PedidoService;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.servicio.ProductoService;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final DireccionEntregaService direccionService;
    private final EpaycoConfig epaycoConfig;
    private final PagoService pagoService;

    private static final List<String> ESTADOS = Arrays.asList(
            "Pendiente", "Procesando", "Enviado", "Entregado", "Cancelado");

    // ── Helper: resuelve Usuario sin importar si es LOCAL u OAuth2 ──
    private Usuario resolverUsuario(Object principal) {
        if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUsuario();
        }
        if (principal instanceof Usuario u) {
            return u;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // Redirige al catálogo
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/crear")
    public String crearForm(RedirectAttributes ra) {
        ra.addFlashAttribute("info",
                "Agrega productos desde el catálogo y usa el carrito para crear tu pedido");
        return "redirect:/productos/catalogo";
    }

    @PostMapping("/crear")
    public String crear(@ModelAttribute PedidoDTO dto,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario usuario = resolverUsuario(principal);
        try {
            Pedido p = pedidoService.crear(dto, usuario);
            ra.addFlashAttribute("exito", "Pedido " + p.getNumeroPedido() + " creado exitosamente");
            return "redirect:/pedidos/mis-pedidos";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/crear";
        }
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos(@AuthenticationPrincipal Object principal,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Usuario usuario = resolverUsuario(principal);
        model.addAttribute("pedidos", pedidoService.listarPorUsuarioConFiltro(usuario.getId(), estado));
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("estados", ESTADOS);
        model.addAttribute("titulo", "Mis Pedidos");
        return "pedidos/mis-pedidos";
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','VENDEDOR')")
    public String todos(@RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal Object principal,
            Model model) {
        Usuario usuario = resolverUsuario(principal);
        model.addAttribute("pagina",
                pedidoService.listarTodos(
                        (estado != null && !estado.isBlank()) ? estado : null,
                        PageRequest.of(page, 15, Sort.by("fechaPedido").descending())));
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("estados", ESTADOS);
        model.addAttribute("titulo", "Todos los Pedidos");
        model.addAttribute("esVendedor", usuario != null && usuario.getRol() == Rol.VENDEDOR);
        return "pedidos/todos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id,
            Model model,
            @AuthenticationPrincipal Object principal) {
        Pedido pedido = pedidoService.buscarPorId(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("estados", ESTADOS);
        model.addAttribute("titulo", "Pedido " + pedido.getNumeroPedido());
        return "pedidos/detalle";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario usuario = resolverUsuario(principal);
        try {
            pedidoService.eliminar(id, usuario);
            ra.addFlashAttribute("exito", "Pedido eliminado correctamente");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/mis-pedidos";
    }

    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String cambiarEstado(@PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String observacion,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario usuario = resolverUsuario(principal);
        try {
            pedidoService.cambiarEstado(id, estado, observacion, usuario);
            ra.addFlashAttribute("exito", "Estado actualizado a: " + estado);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIRMAR PEDIDO — VENDEDOR
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/confirmar/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public String confirmar(@PathVariable Long id,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario usuario = resolverUsuario(principal);
        try {
            pedidoService.cambiarEstado(id, "Procesando", "Confirmado por vendedor", usuario);
            ra.addFlashAttribute("exito", "Pedido confirmado. Ahora está en estado Procesando");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }
    // ═══════════════════════════════════════════════════════════
    // CHECKOUT
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/checkout")
    public String irACheckout(@ModelAttribute PedidoDTO dto,
            @AuthenticationPrincipal Object principal,
            HttpSession session,
            RedirectAttributes ra) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            ra.addFlashAttribute("error", "Debes agregar al menos un producto al carrito");
            return "redirect:/pedidos/crear";
        }
        session.setAttribute("carritoTemporal", dto.getDetalles());
        session.setAttribute("notasTemporal", dto.getNotas());
        return "redirect:/pedidos/checkout";
    }

    @GetMapping("/checkout")
    public String checkout(Model model,
            @AuthenticationPrincipal Object principal,
            HttpSession session,
            RedirectAttributes ra) {

        Usuario usuario = resolverUsuario(principal);

        if (usuario == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para continuar");
            return "redirect:/auth/login";
        }

        @SuppressWarnings("unchecked")
        List<PedidoDTO.ItemDTO> items = (List<PedidoDTO.ItemDTO>) session.getAttribute("carritoTemporal");
        if (items == null || items.isEmpty()) {
            ra.addFlashAttribute("error", "Tu carrito está vacío");
            return "redirect:/pedidos/crear";
        }

        model.addAttribute("usuario", usuario);

        boolean tieneDirecciones = direccionService.usuarioTieneDirecciones(usuario.getId());
        model.addAttribute("tieneDirecciones", tieneDirecciones);

        if (tieneDirecciones) {
            List<DireccionEntrega> dirs = direccionService.obtenerDirecciones(usuario.getId());
            model.addAttribute("direcciones", dirs);
            DireccionEntrega direccionPrincipal = dirs.stream()
                    .filter(DireccionEntrega::getEsPrincipal)
                    .findFirst()
                    .orElse(dirs.get(0));
            model.addAttribute("direccionPrincipal", direccionPrincipal);
        } else {
            model.addAttribute("direccion", new DireccionEntrega());
        }

        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> resumenItems = new ArrayList<>();
        for (var item : items) {
            Producto p = productoService.buscarPorId(item.getIdProducto());
            if (p != null) {
                BigDecimal sub = p.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(item.getCantidad()));
                total = total.add(sub);
                Map<String, Object> map = new HashMap<>();
                map.put("producto", p);
                map.put("cantidad", item.getCantidad());
                map.put("subtotal", sub);
                resumenItems.add(map);
            }
        }

        model.addAttribute("resumenItems", resumenItems);
        model.addAttribute("total", total);
        model.addAttribute("notas", session.getAttribute("notasTemporal"));
        model.addAttribute("titulo", "Finalizar Compra");

        // ── Variables ePayco ──
        model.addAttribute("epaycoPublicKey", epaycoConfig.getPublicKey());
        model.addAttribute("epaycoTest", epaycoConfig.isTest());
        model.addAttribute("epaycoResponseUrl", epaycoConfig.getResponseUrl());
        model.addAttribute("epaycoConfirmationUrl", epaycoConfig.getConfirmationUrl());
        model.addAttribute("totalStr", total.toPlainString());
        model.addAttribute("descripcionPedido",
                "Pedido ACMAFER - " + usuario.getNombreCompleto());
        model.addAttribute("numeroPedidoTemp",
                "TEMP-" + System.currentTimeMillis());

        return "pedidos/checkout";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(@RequestParam Long direccionId,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String notas,
            @AuthenticationPrincipal Object principal,
            HttpSession session,
            RedirectAttributes ra) {

        Usuario usuario = resolverUsuario(principal);

        @SuppressWarnings("unchecked")
        List<PedidoDTO.ItemDTO> items = (List<PedidoDTO.ItemDTO>) session.getAttribute("carritoTemporal");
        if (items == null || items.isEmpty()) {
            ra.addFlashAttribute("error", "Carrito vacío");
            return "redirect:/pedidos/crear";
        }

        try {
            PedidoDTO dto = PedidoDTO.builder()
                    .detalles(items)
                    .metodoPago(metodoPago != null ? metodoPago : "Efectivo")
                    .notas(notas)
                    .direccionEntregaId(direccionId)
                    .build();

            Pedido p = pedidoService.crear(dto, usuario);

            session.removeAttribute("carritoTemporal");
            session.removeAttribute("notasTemporal");

            ra.addFlashAttribute("exito",
                    "Pedido " + p.getNumeroPedido() + " creado exitosamente");
            return "redirect:/pedidos/mis-pedidos";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/checkout";
        }
    }

    @PostMapping("/checkout/guardar-direccion")
    public String guardarDireccion(@ModelAttribute DireccionEntrega direccion,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes ra) {
        Usuario usuario = resolverUsuario(principal);
        try {
            direccion.setUsuario(usuario);
            direccionService.guardar(direccion);
            ra.addFlashAttribute("exito", "Dirección guardada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/pedidos/checkout";
    }

    @PostMapping("/checkout/preparar-pago-epayco")
    @ResponseBody
    public ResponseEntity<?> prepararPagoEpayco(
            @RequestParam Long direccionId,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String notas,
            @AuthenticationPrincipal Object principal,
            HttpSession session) {

        Usuario usuario = resolverUsuario(principal);
        Map<String, Object> resp = new HashMap<>();

        if (usuario == null) {
            resp.put("error", "Debes iniciar sesión para continuar");
            return ResponseEntity.status(401).body(resp);
        }

        @SuppressWarnings("unchecked")
        List<PedidoDTO.ItemDTO> items = (List<PedidoDTO.ItemDTO>) session.getAttribute("carritoTemporal");
        if (items == null || items.isEmpty()) {
            resp.put("error", "Tu carrito está vacío");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            PedidoDTO dto = PedidoDTO.builder()
                    .detalles(items)
                    .metodoPago(metodoPago != null ? metodoPago : "Tarjeta")
                    .notas(notas)
                    .direccionEntregaId(direccionId)
                    .build();

            Pedido p = pedidoService.crear(dto, usuario);
            DireccionEntrega direccion = direccionService.buscarPorId(direccionId); // usa el método que ya tengas

            session.removeAttribute("carritoTemporal");
            session.removeAttribute("notasTemporal");

            return ResponseEntity.ok(pagoService.generarDatosEpayco(p, direccion));

        } catch (BusinessException e) {
            resp.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        } catch (Exception e) {
            resp.put("error", "Error inesperado al preparar el pago");
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    @PostMapping("/checkout/actualizar-direccion/{id}")
    public String actualizarDireccion(@PathVariable Long id,
            @ModelAttribute DireccionEntrega datos,
            RedirectAttributes ra) {
        try {
            direccionService.actualizar(id, datos);
            ra.addFlashAttribute("exito", "Dirección actualizada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/pedidos/checkout";
    }

    // ═══════════════════════════════════════════════════════════
    // AJAX — Crear pedido desde drawer
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/crear-desde-drawer")
    @ResponseBody
    public ResponseEntity<?> crearDesdeDrawer(@RequestBody PedidoDTO dto,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = resolverUsuario(principal);
        try {
            if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El carrito está vacío");
                return ResponseEntity.badRequest().body(error);
            }

            Pedido p = pedidoService.crear(dto, usuario);

            Map<String, String> response = new HashMap<>();
            response.put("numeroPedido", p.getNumeroPedido());
            response.put("mensaje", "Pedido creado exitosamente");
            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error inesperado al crear el pedido");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}