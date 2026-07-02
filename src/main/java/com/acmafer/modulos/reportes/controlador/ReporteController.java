package com.acmafer.modulos.reportes.controlador;

import com.acmafer.comun.seguridad.CustomOAuth2User;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.reportes.servicio.ReporteService;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String index(Model model) {
        model.addAttribute("titulo", "Reportes");

        // ── KPIs para el strip superior ──────────────────────────────────────
        model.addAttribute("kpiTotalProductos",    productoRepo.count());
        model.addAttribute("kpiTotalPedidos",      pedidoRepo.count());
        model.addAttribute("kpiPendientes",        pedidoRepo.countByEstado("Pendiente"));
        model.addAttribute("kpiStockBajo", productoRepo.countConStockBajo());
        model.addAttribute("kpiTotalVentas",       pedidoRepo.sumTotalVentas());

        return "reportes/index";
    }

    // ═══════════════════════════════════════════════════════════════
    // VENDEDOR — Panel de reportes simplificado (solo lectura de ventas)
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/ventas")
    @PreAuthorize("hasRole('VENDEDOR')")
    public String reportesVentas(Model model, @AuthenticationPrincipal Object principal) {
        model.addAttribute("titulo", "Mis Reportes de Ventas");
        model.addAttribute("kpiPendientes",  pedidoRepo.countByEstado("Pendiente"));
        model.addAttribute("kpiProcesando",  pedidoRepo.countByEstado("Procesando"));
        model.addAttribute("kpiEntregados",  pedidoRepo.countByEstado("Entregado"));
        model.addAttribute("kpiTotalVentas", pedidoRepo.sumTotalVentas());
        return "reportes/ventas-vendedor";
    }

    @GetMapping("/pdf/ventas-vendedor")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<byte[]> pdfVentasVendedor(@AuthenticationPrincipal Object principal) {
        Usuario vendedor = resolverUsuario(principal);
        byte[] pdf = reporteService.generarPdfVentasVendedor(vendedor);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=mis_ventas_" + LocalDateTime.now().format(TS) + ".pdf")
            .body(pdf);
    }

    // ── PDF — ADMIN / SUPERVISOR ──────────────────────────────────────────────

    @GetMapping("/pdf/pedidos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<byte[]> pdfPedidos(@RequestParam(required = false) String estado) {
        byte[] pdf = reporteService.generarPdfPedidos(estado);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=pedidos_" + LocalDateTime.now().format(TS) + ".pdf")
            .body(pdf);
    }

    @GetMapping("/pdf/productos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<byte[]> pdfProductos() {
        byte[] pdf = reporteService.generarPdfProductos();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=productos_" + LocalDateTime.now().format(TS) + ".pdf")
            .body(pdf);
    }

    @GetMapping("/pdf/dashboard")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<byte[]> pdfDashboard() {
        byte[] pdf = reporteService.generarPdfDashboard();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=dashboard_ejecutivo_" + LocalDateTime.now().format(TS) + ".pdf")
            .body(pdf);
    }

    // ── Excel ────────────────────────────────────────────────────────────────

    @GetMapping("/excel/productos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> excelProductos() {
        byte[] excel = reporteService.exportarProductosExcel();
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=productos_" + LocalDateTime.now().format(TS) + ".xlsx")
            .body(excel);
    }

    @GetMapping("/excel/template")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> templateExcel() {
        byte[] t = reporteService.generarTemplateExcel();
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=plantilla_productos.xlsx")
            .body(t);
    }

    @PostMapping("/excel/importar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String importarExcel(@RequestParam("archivo") MultipartFile archivo,
                                RedirectAttributes ra) {
        if (archivo.isEmpty()) {
            ra.addFlashAttribute("error", "Selecciona un archivo Excel (.xlsx)");
            return "redirect:/reportes";
        }
        try {
            com.acmafer.modulos.usuarios.entidad.Usuario u =
                (com.acmafer.modulos.usuarios.entidad.Usuario)
                    org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
            int n = reporteService.importarProductosDesdeExcel(archivo.getInputStream(), u);
            ra.addFlashAttribute("exito", n + " productos importados correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error importando: " + e.getMessage());
        }
        return "redirect:/reportes";
    }
}