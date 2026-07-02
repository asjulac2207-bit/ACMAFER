package com.acmafer.modulos.reportes.servicio;

import com.acmafer.modulos.pedidos.entidad.Pedido;
import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.entidad.Categoria;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BaseColor ACMAFER_BLUE = new BaseColor(27, 43, 75);
    private static final BaseColor ACMAFER_ORANGE = new BaseColor(230, 126, 34);
    private static final BaseColor ACMAFER_RED = new BaseColor(192, 57, 43);
    private static final BaseColor ACMAFER_GREEN = new BaseColor(39, 174, 96);

    // ═══════════════════════════════════════════════════════════════
    // Shared header builder — encabezado de marca uniforme
    // ═══════════════════════════════════════════════════════════════
    /**
     * Añade al documento:
     * • Logo bicolor ACM|AFER
     * • Subtítulo "Excelencia en Fundición Industrial"
     * • Fecha de generación alineada a la derecha
     * • Línea separadora naranja
     * • Título del reporte
     * • Salto de línea
     */
    private void addReportHeader(Document doc, String reportTitle) throws DocumentException {
        com.itextpdf.text.Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, ACMAFER_BLUE);
        com.itextpdf.text.Font brandAccentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, ACMAFER_ORANGE);
        com.itextpdf.text.Font subBrandFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
        com.itextpdf.text.Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);

        PdfPTable brandTable = new PdfPTable(2);
        brandTable.setWidthPercentage(100);
        brandTable.setWidths(new float[] { 60f, 40f });
        brandTable.setSpacingAfter(4);

        // Celda izquierda: "ACM" (azul) + "AFER" (naranja)
        Phrase brandPhrase = new Phrase();
        brandPhrase.add(new Chunk("ACM", brandFont));
        brandPhrase.add(new Chunk("AFER", brandAccentFont));
        PdfPCell brandCell = new PdfPCell();
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.addElement(new Paragraph(brandPhrase));
        brandCell.addElement(new Paragraph("Excelencia en Fundición Industrial", subBrandFont));
        brandTable.addCell(brandCell);

        // Celda derecha: fecha generación
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        Paragraph genP = new Paragraph("Descarga generada el\n" + LocalDateTime.now().format(FMT), metaFont);
        genP.setAlignment(Element.ALIGN_RIGHT);
        dateCell.addElement(genP);
        brandTable.addCell(dateCell);

        doc.add(brandTable);
        doc.add(new Chunk(new LineSeparator(1f, 100, ACMAFER_ORANGE, Element.ALIGN_CENTER, -2)));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(reportTitle, titleFont));
        doc.add(Chunk.NEWLINE);
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF: Reporte de pedidos
    // ═══════════════════════════════════════════════════════════════
    public byte[] generarPdfPedidos(String filtroEstado) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new ReportePageEvent());
            doc.open();

            com.itextpdf.text.Font hdrFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

            addReportHeader(doc, "Reporte de Pedidos");

            // KPIs rápidos de pedidos
            List<Pedido> pedidos = pedidoRepo.findAllByOrderByFechaPedidoDesc(PageRequest.of(0, 500)).getContent();
            long pendientes = pedidos.stream().filter(p -> "Pendiente".equals(p.getEstado())).count();
            long entregados = pedidos.stream().filter(p -> "Entregado".equals(p.getEstado())).count();
            BigDecimal totalVentas = pedidos.stream()
                    .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            com.itextpdf.text.Font kpiValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, ACMAFER_BLUE);
            com.itextpdf.text.Font kpiLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(14);
            addKpiCardCell(kpiTable, String.valueOf(pedidos.size()), "Total Pedidos", kpiValFont, kpiLabelFont,
                    ACMAFER_BLUE);
            addKpiCardCell(kpiTable, String.valueOf(pendientes), "Pendientes", kpiValFont, kpiLabelFont,
                    ACMAFER_ORANGE);
            addKpiCardCell(kpiTable, String.valueOf(entregados), "Entregados", kpiValFont, kpiLabelFont, ACMAFER_GREEN);
            addKpiCardCell(kpiTable, "$" + formatMiles(totalVentas), "Total Facturado", kpiValFont, kpiLabelFont,
                    ACMAFER_RED);
            doc.add(kpiTable);

            // Tabla de pedidos
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 18f, 15f, 30f, 17f, 20f });
            tabla.setHeaderRows(1);

            for (String h : new String[] { "N° Pedido", "Estado", "Cliente", "Total", "Fecha" }) {
                PdfPCell cell = new PdfPCell(new Phrase(h, hdrFont));
                cell.setBackgroundColor(ACMAFER_BLUE);
                cell.setPadding(7);
                tabla.addCell(cell);
            }

            boolean alt = false;
            for (Pedido p : pedidos) {
                if (filtroEstado != null && !filtroEstado.isBlank() && !filtroEstado.equals(p.getEstado()))
                    continue;
                BaseColor bg = alt ? new BaseColor(245, 248, 252) : BaseColor.WHITE;
                addCell(tabla, p.getNumeroPedido(), cellFont, bg);
                addEstadoPedidoBadge(tabla, p.getEstado(), bg);
                addCell(tabla, p.getUsuario() != null ? p.getUsuario().getNombreCompleto() : "-", cellFont, bg);
                addCell(tabla,
                        p.getTotal() != null ? "$" + formatMiles(p.getTotal()) : "-",
                        cellFont, bg);
                addCell(tabla,
                        p.getFechaPedido() != null ? p.getFechaPedido().format(FMT) : "-",
                        cellFont, bg);
                alt = !alt;
            }
            doc.add(tabla);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(
                    "Total de registros: " + pedidos.size()
                            + "  •  Documento generado automáticamente por el sistema ACMAFER",
                    metaFont));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF pedidos", e);
            throw new RuntimeException("Error generando PDF: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF: Reporte de productos — diseño corporativo ACMAFER
    // ═══════════════════════════════════════════════════════════════
    public byte[] generarPdfProductos() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 95, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new ReportePageEvent());
            doc.open();

            com.itextpdf.text.Font hdrFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font cellBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,
                    BaseColor.DARK_GRAY);
            com.itextpdf.text.Font alertFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, ACMAFER_RED);
            com.itextpdf.text.Font kpiValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, ACMAFER_BLUE);
            com.itextpdf.text.Font kpiLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
            com.itextpdf.text.Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

            addReportHeader(doc, "Reporte de Inventario de Productos");

            // KPIs
            List<Producto> productos = productoRepo.findAll();
            long disponibles = productos.stream().filter(p -> "Disponible".equals(p.getEstado())).count();
            long stockBajoCount = productos.stream().filter(Producto::tieneStockBajo).count();
            BigDecimal valorInventario = productos.stream()
                    .map(p -> p.getPrecioUnitario() != null
                            ? p.getPrecioUnitario().multiply(BigDecimal.valueOf(p.getStockActual()))
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(14);
            addKpiCardCell(kpiTable, String.valueOf(productos.size()), "Total Productos", kpiValFont, kpiLabelFont,
                    ACMAFER_BLUE);
            addKpiCardCell(kpiTable, String.valueOf(disponibles), "Disponibles", kpiValFont, kpiLabelFont,
                    ACMAFER_GREEN);
            addKpiCardCell(kpiTable, String.valueOf(stockBajoCount), "Stock Bajo", kpiValFont, kpiLabelFont,
                    ACMAFER_RED);
            addKpiCardCell(kpiTable, "$" + formatMiles(valorInventario), "Valor Inventario", kpiValFont, kpiLabelFont,
                    ACMAFER_ORANGE);
            doc.add(kpiTable);

            // Tabla principal
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 10f, 28f, 16f, 15f, 13f, 14f });
            tabla.setHeaderRows(1);

            for (String h : new String[] { "Código", "Producto", "Categoría", "Precio Unit.", "Stock", "Estado" }) {
                PdfPCell cell = new PdfPCell(new Phrase(h, hdrFont));
                cell.setBackgroundColor(ACMAFER_BLUE);
                cell.setPadding(7);
                tabla.addCell(cell);
            }

            boolean alt = false;
            for (Producto p : productos) {
                BaseColor bg = alt ? new BaseColor(245, 248, 252) : BaseColor.WHITE;
                boolean stockBajo = p.tieneStockBajo();

                addCell(tabla, p.getCodigo(), cellBoldFont, bg);
                addCell(tabla, p.getNombre(), cellFont, bg);
                addCell(tabla, p.getCategoria() != null ? p.getCategoria().getNombre() : "-", cellFont, bg);
                addCell(tabla, p.getPrecioUnitario() != null ? "$" + formatMiles(p.getPrecioUnitario()) : "-", cellFont,
                        bg);

                PdfPCell stockCell = new PdfPCell(
                        new Phrase(String.valueOf(p.getStockActual()), stockBajo ? alertFont : cellFont));
                stockCell.setBackgroundColor(stockBajo ? new BaseColor(253, 235, 235) : bg);
                stockCell.setPadding(6);
                stockCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stockCell.setBorderColor(new BaseColor(228, 228, 233));
                tabla.addCell(stockCell);

                addEstadoBadge(tabla, p.getEstado(), bg);
                alt = !alt;
            }
            doc.add(tabla);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(
                    "Total de registros: " + productos.size()
                            + "  •  Documento generado automáticamente por el sistema ACMAFER",
                    metaFont));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF productos", e);
            throw new RuntimeException("Error generando PDF: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF: Dashboard ejecutivo
    // ═══════════════════════════════════════════════════════════════
    public byte[] generarPdfDashboard() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new ReportePageEvent());
            doc.open();

            com.itextpdf.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, ACMAFER_ORANGE);
            com.itextpdf.text.Font kpiValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, ACMAFER_BLUE);
            com.itextpdf.text.Font kpiLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
            com.itextpdf.text.Font hdrFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

            addReportHeader(doc, "Dashboard Ejecutivo");

            // ── Indicadores Clave ──
            doc.add(new Paragraph("■ Indicadores Clave", sectionFont));
            doc.add(Chunk.NEWLINE);

            long totalPedidos = pedidoRepo.count();
            long pedidosPendientes = pedidoRepo.countByEstado("Pendiente");
            long totalProductos = productoRepo.count();
            BigDecimal ventas = pedidoRepo.sumTotalVentas();
            long totalUsuarios = usuarioRepo.count();
            int stockBajo = productoRepo.findConStockBajo().size();

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(14);
            addKpiCardCell(kpiTable, String.valueOf(totalPedidos),
                    "Total Pedidos", kpiValFont, kpiLabelFont, ACMAFER_BLUE);
            addKpiCardCell(kpiTable, String.valueOf(pedidosPendientes),
                    "Pedidos Pendientes", kpiValFont, kpiLabelFont, ACMAFER_ORANGE);
            addKpiCardCell(kpiTable,
                    ventas != null ? "$" + formatMiles(ventas) : "$0",
                    "Total Ventas", kpiValFont, kpiLabelFont, ACMAFER_GREEN);
            addKpiCardCell(kpiTable, String.valueOf(totalProductos),
                    "Productos", kpiValFont, kpiLabelFont, ACMAFER_BLUE);
            addKpiCardCell(kpiTable, String.valueOf(totalUsuarios),
                    "Usuarios", kpiValFont, kpiLabelFont, ACMAFER_BLUE);
            addKpiCardCell(kpiTable, String.valueOf(stockBajo),
                    "Stock Bajo", kpiValFont, kpiLabelFont, ACMAFER_RED);
            doc.add(kpiTable);

            // ── Top 5 más vendidos ──
            doc.add(new Paragraph("■ Top 5 Productos Más Vendidos", sectionFont));
            doc.add(Chunk.NEWLINE);

            List<Producto> top = productoRepo.findTopVendidos(PageRequest.of(0, 5));
            PdfPTable topTable = new PdfPTable(3);
            topTable.setWidthPercentage(100);
            topTable.setHeaderRows(1);
            for (String h : new String[] { "Producto", "Código", "Ventas Totales" }) {
                PdfPCell c = new PdfPCell(new Phrase(h, hdrFont));
                c.setBackgroundColor(ACMAFER_BLUE);
                c.setPadding(7);
                topTable.addCell(c);
            }
            boolean alt = false;
            for (Producto p : top) {
                BaseColor bg = alt ? new BaseColor(245, 248, 252) : BaseColor.WHITE;
                addCell(topTable, p.getNombre(), cellFont, bg);
                addCell(topTable, p.getCodigo(), cellFont, bg);
                addCell(topTable, String.valueOf(p.getVentasTotales()), cellFont, bg);
                alt = !alt;
            }
            doc.add(topTable);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(
                    "Documento generado automáticamente por el sistema ACMAFER",
                    metaFont));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF dashboard", e);
            throw new RuntimeException("Error generando PDF dashboard: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXCEL: Importar carga masiva
    // ═══════════════════════════════════════════════════════════════
    public int importarProductosDesdeExcel(java.io.InputStream inputStream, Usuario responsable) {
        int importados = 0;
        try (XSSFWorkbook wb = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                try {
                    Producto p = new Producto();
                    p.setCodigo(getCellStr(row, 0));
                    p.setNombre(getCellStr(row, 1));
                    p.setDescripcion(getCellStr(row, 2));
                    Long catId = (long) getCellNum(row, 3);
                    p.setCategoria(new Categoria());
                    p.getCategoria().setId(catId);
                    p.setPrecioUnitario(BigDecimal.valueOf(getCellNum(row, 4)));
                    p.setStockActual((int) getCellNum(row, 5));
                    p.setStockMinimo((int) getCellNum(row, 6));
                    p.setEstado(getCellStr(row, 7));
                    if (p.getEstado() == null || p.getEstado().isBlank())
                        p.setEstado("Disponible");
                    productoRepo.save(p);
                    importados++;
                } catch (Exception ex) {
                    log.warn("Error en fila {} de Excel: {}", i, ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo Excel: " + e.getMessage());
        }
        return importados;
    }

    // ── Helpers de tabla / celdas ───────────────────────────────────────────
    private void addCell(PdfPTable t, String val, com.itextpdf.text.Font f, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "", f));
        c.setBackgroundColor(bg);
        c.setPadding(6);
        c.setBorderColor(new BaseColor(228, 228, 233));
        t.addCell(c);
    }

    /** Tarjeta KPI con borde de acento lateral (compartida por todos los PDFs). */
    private void addKpiCardCell(PdfPTable t, String val, String label,
            com.itextpdf.text.Font vFont, com.itextpdf.text.Font lFont, BaseColor accent) {
        PdfPCell c = new PdfPCell();
        c.setPadding(10);
        c.setBackgroundColor(new BaseColor(250, 250, 252));
        c.setBorderColor(new BaseColor(228, 228, 233));
        c.setBorderWidthLeft(3f);
        c.setBorderColorLeft(accent);
        c.addElement(new Paragraph(val, vFont));
        Paragraph labP = new Paragraph(label, lFont);
        labP.setSpacingBefore(2);
        c.addElement(labP);
        t.addCell(c);
    }

    /** Insignia de color para la columna Estado del inventario de productos. */
    private void addEstadoBadge(PdfPTable t, String estado, BaseColor rowBg) {
        BaseColor badgeBg;
        BaseColor badgeText;
        switch (estado != null ? estado : "") {
            case "Disponible" -> {
                badgeBg = new BaseColor(223, 246, 233);
                badgeText = ACMAFER_GREEN;
            }
            case "Agotado" -> {
                badgeBg = new BaseColor(253, 235, 235);
                badgeText = ACMAFER_RED;
            }
            case "Descontinuado" -> {
                badgeBg = new BaseColor(238, 238, 238);
                badgeText = BaseColor.GRAY;
            }
            default -> {
                badgeBg = rowBg;
                badgeText = BaseColor.DARK_GRAY;
            }
        }
        com.itextpdf.text.Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, badgeText);
        PdfPCell cell = new PdfPCell(new Phrase(estado != null ? estado.toUpperCase() : "-", badgeFont));
        cell.setBackgroundColor(badgeBg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(new BaseColor(228, 228, 233));
        t.addCell(cell);
    }

    /** Insignia de color para la columna Estado del reporte de pedidos. */
    private void addEstadoPedidoBadge(PdfPTable t, String estado, BaseColor rowBg) {
        BaseColor badgeBg;
        BaseColor badgeText;
        switch (estado != null ? estado : "") {
            case "Pendiente" -> {
                badgeBg = new BaseColor(255, 243, 205);
                badgeText = new BaseColor(133, 77, 14);
            }
            case "En proceso" -> {
                badgeBg = new BaseColor(219, 234, 254);
                badgeText = new BaseColor(30, 64, 175);
            }
            case "Entregado" -> {
                badgeBg = new BaseColor(223, 246, 233);
                badgeText = ACMAFER_GREEN;
            }
            case "Cancelado" -> {
                badgeBg = new BaseColor(253, 235, 235);
                badgeText = ACMAFER_RED;
            }
            default -> {
                badgeBg = rowBg;
                badgeText = BaseColor.DARK_GRAY;
            }
        }
        com.itextpdf.text.Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, badgeText);
        PdfPCell cell = new PdfPCell(new Phrase(estado != null ? estado.toUpperCase() : "-", badgeFont));
        cell.setBackgroundColor(badgeBg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(new BaseColor(228, 228, 233));
        t.addCell(cell);
    }

    /** Formato de moneda es-CO con separador de miles (ej. 120.000). */
    private String formatMiles(BigDecimal val) {
        if (val == null)
            return "0";
        return NumberFormat.getInstance(new Locale("es", "CO"))
                .format(val.setScale(0, RoundingMode.HALF_UP));
    }

    private String getCellStr(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null)
            return "";
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) c.getNumericCellValue());
            default -> "";
        };
    }

    private double getCellNum(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null)
            return 0;
        return switch (c.getCellType()) {
            case NUMERIC -> c.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(c.getStringCellValue());
                } catch (Exception e) {
                    yield 0;
                }
            }
            default -> 0;
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // Evento de página: franja de marca arriba + pie con numeración
    // (aplicado a TODOS los PDFs)
    // ═══════════════════════════════════════════════════════════════
    private class ReportePageEvent extends PdfPageEventHelper {
        private PdfTemplate totalPagesTemplate;
        private final com.itextpdf.text.Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        private final com.itextpdf.text.Font footerBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8,
                ACMAFER_BLUE);

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPagesTemplate = writer.getDirectContent().createTemplate(40, 15);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float pageWidth = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();

            // Franja superior azul + acento naranja
            cb.saveState();
            cb.setColorFill(ACMAFER_BLUE);
            cb.rectangle(0, pageHeight - 8, pageWidth, 8);
            cb.fill();
            cb.setColorFill(ACMAFER_ORANGE);
            cb.rectangle(0, pageHeight - 11, pageWidth, 3);
            cb.fill();
            cb.restoreState();

            // Línea de pie
            cb.saveState();
            cb.setColorStroke(new BaseColor(220, 220, 220));
            cb.setLineWidth(0.5f);
            cb.moveTo(36, 38);
            cb.lineTo(pageWidth - 36, 38);
            cb.stroke();
            cb.restoreState();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("ACMAFER — Sistema de Gestión Industrial", footerBoldFont),
                    36, 24, 0);

            String pageStr = "Página " + writer.getPageNumber() + " de ";
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(pageStr, footerFont),
                    pageWidth - 76, 24, 0);

            cb.addTemplate(totalPagesTemplate,
                    pageWidth - 76 + getTextWidth(pageStr, footerFont), 22);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            try {
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                totalPagesTemplate.beginText();
                totalPagesTemplate.setFontAndSize(bf, 8);
                totalPagesTemplate.setColorFill(BaseColor.GRAY);
                totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
                totalPagesTemplate.endText();
            } catch (Exception ex) {
                log.warn("No se pudo generar la numeración total de páginas: {}", ex.getMessage());
            }
        }

        private float getTextWidth(String text, com.itextpdf.text.Font f) {
            return f.getBaseFont() != null
                    ? f.getBaseFont().getWidthPoint(text, f.getSize())
                    : text.length() * 4.5f;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXCEL: Exportar productos — diseño corporativo ACMAFER
    // ═══════════════════════════════════════════════════════════════
    public byte[] exportarProductosExcel() {
        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Productos ACMAFER");
            sheet.setTabColor(new XSSFColor(new byte[] { 27, 43, 75 }, null)); // ACMAFER_BLUE

            // ── Paleta corporativa ────────────────────────────────────────
            XSSFColor blue = new XSSFColor(new byte[] { (byte) 27, (byte) 43, (byte) 75 }, null);
            XSSFColor orange = new XSSFColor(new byte[] { (byte) 230, (byte) 126, (byte) 34 }, null);
            XSSFColor green = new XSSFColor(new byte[] { (byte) 39, (byte) 174, (byte) 96 }, null);
            XSSFColor red = new XSSFColor(new byte[] { (byte) 192, (byte) 57, (byte) 43 }, null);
            XSSFColor gray = new XSSFColor(new byte[] { (byte) 120, (byte) 120, (byte) 120 }, null);
            XSSFColor lightBlue = new XSSFColor(new byte[] { (byte) 245, (byte) 248, (byte) 252 }, null);
            XSSFColor lightGreen = new XSSFColor(new byte[] { (byte) 223, (byte) 246, (byte) 233 }, null);
            XSSFColor lightRed = new XSSFColor(new byte[] { (byte) 253, (byte) 235, (byte) 235 }, null);
            XSSFColor lightGray = new XSSFColor(new byte[] { (byte) 238, (byte) 238, (byte) 238 }, null);
            XSSFColor borderColor = new XSSFColor(new byte[] { (byte) 228, (byte) 228, (byte) 233 }, null);
            XSSFColor white = new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null);
            XSSFColor darkGray = new XSSFColor(new byte[] { (byte) 64, (byte) 64, (byte) 64 }, null);
            XSSFColor softBg = new XSSFColor(new byte[] { (byte) 250, (byte) 250, (byte) 252 }, null);

            // ── Anchos de columna ─────────────────────────────────────────
            int[] colWidths = { 12 * 256, 14 * 256, 30 * 256, 40 * 256, 18 * 256, 16 * 256, 10 * 256, 12 * 256,
                    14 * 256, 16 * 256, 22 * 256 };
            for (int i = 0; i < colWidths.length; i++)
                sheet.setColumnWidth(i, colWidths[i]);

            // ── Estilos reutilizables ─────────────────────────────────────
            // Marca: "ACMAFER" en la franja azul
            XSSFCellStyle brandStyle = wb.createCellStyle();
            XSSFFont brandFont = wb.createFont();
            brandFont.setBold(true);
            brandFont.setFontHeightInPoints((short) 20);
            brandFont.setColor(white);
            brandStyle.setFont(brandFont);
            brandStyle.setFillForegroundColor(blue);
            brandStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            brandStyle.setAlignment(HorizontalAlignment.LEFT);
            brandStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Subtítulo franja azul
            XSSFCellStyle subStyle = wb.createCellStyle();
            XSSFFont subFont = wb.createFont();
            subFont.setFontHeightInPoints((short) 9);
            subFont.setColor(orange);
            subStyle.setFont(subFont);
            subStyle.setFillForegroundColor(blue);
            subStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subStyle.setAlignment(HorizontalAlignment.LEFT);
            subStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Fecha de generación (lado derecho de la franja)
            XSSFCellStyle dateStyle = wb.createCellStyle();
            XSSFFont dateFont = wb.createFont();
            dateFont.setFontHeightInPoints((short) 8);
            dateFont.setColor(orange);
            dateStyle.setFont(dateFont);
            dateStyle.setFillForegroundColor(blue);
            dateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dateStyle.setAlignment(HorizontalAlignment.RIGHT);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Acento naranja (franja decorativa)
            XSSFCellStyle accentStyle = wb.createCellStyle();
            accentStyle.setFillForegroundColor(orange);
            accentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Título del reporte
            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleFont.setColor(blue);
            titleStyle.setFont(titleFont);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // KPI — valor grande
            XSSFCellStyle kpiValStyle = wb.createCellStyle();
            XSSFFont kpiValFont = wb.createFont();
            kpiValFont.setBold(true);
            kpiValFont.setFontHeightInPoints((short) 18);
            kpiValFont.setColor(blue);
            kpiValStyle.setFont(kpiValFont);
            kpiValStyle.setFillForegroundColor(softBg);
            kpiValStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiValStyle.setAlignment(HorizontalAlignment.CENTER);
            kpiValStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // KPI — valor naranja
            XSSFCellStyle kpiOrangeValStyle = wb.createCellStyle();
            XSSFFont kpiOrangeFont = wb.createFont();
            kpiOrangeFont.setBold(true);
            kpiOrangeFont.setFontHeightInPoints((short) 18);
            kpiOrangeFont.setColor(orange);
            kpiOrangeValStyle.setFont(kpiOrangeFont);
            kpiOrangeValStyle.setFillForegroundColor(softBg);
            kpiOrangeValStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiOrangeValStyle.setAlignment(HorizontalAlignment.CENTER);
            kpiOrangeValStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // KPI — valor verde
            XSSFCellStyle kpiGreenValStyle = wb.createCellStyle();
            XSSFFont kpiGreenFont = wb.createFont();
            kpiGreenFont.setBold(true);
            kpiGreenFont.setFontHeightInPoints((short) 18);
            kpiGreenFont.setColor(green);
            kpiGreenValStyle.setFont(kpiGreenFont);
            kpiGreenValStyle.setFillForegroundColor(softBg);
            kpiGreenValStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiGreenValStyle.setAlignment(HorizontalAlignment.CENTER);
            kpiGreenValStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // KPI — valor rojo
            XSSFCellStyle kpiRedValStyle = wb.createCellStyle();
            XSSFFont kpiRedFont = wb.createFont();
            kpiRedFont.setBold(true);
            kpiRedFont.setFontHeightInPoints((short) 18);
            kpiRedFont.setColor(red);
            kpiRedValStyle.setFont(kpiRedFont);
            kpiRedValStyle.setFillForegroundColor(softBg);
            kpiRedValStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiRedValStyle.setAlignment(HorizontalAlignment.CENTER);
            kpiRedValStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // KPI — etiqueta
            XSSFCellStyle kpiLabelStyle = wb.createCellStyle();
            XSSFFont kpiLabelFont = wb.createFont();
            kpiLabelFont.setFontHeightInPoints((short) 8);
            kpiLabelFont.setColor(gray);
            kpiLabelStyle.setFont(kpiLabelFont);
            kpiLabelStyle.setFillForegroundColor(softBg);
            kpiLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            kpiLabelStyle.setAlignment(HorizontalAlignment.CENTER);
            kpiLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            kpiLabelStyle.setBorderBottom(BorderStyle.THIN);
            kpiLabelStyle.setBottomBorderColor(borderColor);

            // Encabezado de tabla (azul, blanco, bold)
            XSSFCellStyle hdrStyle = wb.createCellStyle();
            XSSFFont hdrFont = wb.createFont();
            hdrFont.setBold(true);
            hdrFont.setColor(white);
            hdrFont.setFontHeightInPoints((short) 9);
            hdrStyle.setFont(hdrFont);
            hdrStyle.setFillForegroundColor(blue);
            hdrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hdrStyle.setAlignment(HorizontalAlignment.CENTER);
            hdrStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            hdrStyle.setBorderBottom(BorderStyle.MEDIUM);
            hdrStyle.setBottomBorderColor(orange);
            hdrStyle.setBorderTop(BorderStyle.THIN);
            hdrStyle.setTopBorderColor(blue);
            hdrStyle.setBorderLeft(BorderStyle.THIN);
            hdrStyle.setLeftBorderColor(blue);
            hdrStyle.setBorderRight(BorderStyle.THIN);
            hdrStyle.setRightBorderColor(blue);

            // Celda de datos — fila blanca
            XSSFCellStyle cellWhite = createDataCellStyle(wb, white, darkGray, borderColor);
            // Celda de datos — fila alternada
            XSSFCellStyle cellAlt = createDataCellStyle(wb, lightBlue, darkGray, borderColor);

            // Badge Disponible
            XSSFCellStyle badgeGreen = wb.createCellStyle();
            XSSFFont bgf = wb.createFont();
            bgf.setBold(true);
            bgf.setFontHeightInPoints((short) 8);
            bgf.setColor(green);
            badgeGreen.setFont(bgf);
            badgeGreen.setFillForegroundColor(lightGreen);
            badgeGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            badgeGreen.setAlignment(HorizontalAlignment.CENTER);
            badgeGreen.setVerticalAlignment(VerticalAlignment.CENTER);
            badgeGreen.setBorderBottom(BorderStyle.THIN);
            badgeGreen.setBottomBorderColor(borderColor);
            badgeGreen.setBorderTop(BorderStyle.THIN);
            badgeGreen.setTopBorderColor(borderColor);
            badgeGreen.setBorderLeft(BorderStyle.THIN);
            badgeGreen.setLeftBorderColor(borderColor);
            badgeGreen.setBorderRight(BorderStyle.THIN);
            badgeGreen.setRightBorderColor(borderColor);

            // Badge Agotado
            XSSFCellStyle badgeRed = wb.createCellStyle();
            XSSFFont brf = wb.createFont();
            brf.setBold(true);
            brf.setFontHeightInPoints((short) 8);
            brf.setColor(red);
            badgeRed.setFont(brf);
            badgeRed.setFillForegroundColor(lightRed);
            badgeRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            badgeRed.setAlignment(HorizontalAlignment.CENTER);
            badgeRed.setVerticalAlignment(VerticalAlignment.CENTER);
            badgeRed.setBorderBottom(BorderStyle.THIN);
            badgeRed.setBottomBorderColor(borderColor);
            badgeRed.setBorderTop(BorderStyle.THIN);
            badgeRed.setTopBorderColor(borderColor);
            badgeRed.setBorderLeft(BorderStyle.THIN);
            badgeRed.setLeftBorderColor(borderColor);
            badgeRed.setBorderRight(BorderStyle.THIN);
            badgeRed.setRightBorderColor(borderColor);

            // Badge Descontinuado
            XSSFCellStyle badgeGray = wb.createCellStyle();
            XSSFFont bgrf = wb.createFont();
            bgrf.setBold(true);
            bgrf.setFontHeightInPoints((short) 8);
            bgrf.setColor(gray);
            badgeGray.setFont(bgrf);
            badgeGray.setFillForegroundColor(lightGray);
            badgeGray.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            badgeGray.setAlignment(HorizontalAlignment.CENTER);
            badgeGray.setVerticalAlignment(VerticalAlignment.CENTER);
            badgeGray.setBorderBottom(BorderStyle.THIN);
            badgeGray.setBottomBorderColor(borderColor);
            badgeGray.setBorderTop(BorderStyle.THIN);
            badgeGray.setTopBorderColor(borderColor);
            badgeGray.setBorderLeft(BorderStyle.THIN);
            badgeGray.setLeftBorderColor(borderColor);
            badgeGray.setBorderRight(BorderStyle.THIN);
            badgeGray.setRightBorderColor(borderColor);

            // Stock bajo
            XSSFCellStyle stockAlertStyle = createDataCellStyle(wb, lightRed, red, borderColor);
            XSSFFont saf = wb.createFont();
            saf.setBold(true);
            saf.setFontHeightInPoints((short) 9);
            saf.setColor(red);
            stockAlertStyle.setFont(saf);
            stockAlertStyle.setAlignment(HorizontalAlignment.CENTER);

            // Celda de stock normal centrada
            XSSFCellStyle stockNormalWhite = createDataCellStyle(wb, white, darkGray, borderColor);
            stockNormalWhite.setAlignment(HorizontalAlignment.CENTER);
            XSSFCellStyle stockNormalAlt = createDataCellStyle(wb, lightBlue, darkGray, borderColor);
            stockNormalAlt.setAlignment(HorizontalAlignment.CENTER);

            // Precio alineado a la derecha
            XSSFCellStyle priceWhite = createDataCellStyle(wb, white, darkGray, borderColor);
            priceWhite.setAlignment(HorizontalAlignment.RIGHT);
            XSSFCellStyle priceAlt = createDataCellStyle(wb, lightBlue, darkGray, borderColor);
            priceAlt.setAlignment(HorizontalAlignment.RIGHT);

            // Código bold
            XSSFCellStyle codeWhite = createDataCellStyle(wb, white, darkGray, borderColor);
            XSSFFont cf = wb.createFont();
            cf.setBold(true);
            cf.setFontHeightInPoints((short) 9);
            cf.setColor(blue);
            codeWhite.setFont(cf);
            XSSFCellStyle codeAlt = createDataCellStyle(wb, lightBlue, darkGray, borderColor);
            codeAlt.setFont(cf);

            // Pie de documento
            XSSFCellStyle footerStyle = wb.createCellStyle();
            XSSFFont footerFont = wb.createFont();
            footerFont.setItalic(true);
            footerFont.setFontHeightInPoints((short) 8);
            footerFont.setColor(gray);
            footerStyle.setFont(footerFont);
            footerStyle.setFillForegroundColor(softBg);
            footerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            footerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // ── Construcción del encabezado ───────────────────────────────
            int rowIdx = 0;

            // Fila 0: Franja azul — logo "ACMAFER"
            Row r0 = sheet.createRow(rowIdx++);
            r0.setHeightInPoints(30);
            Cell brandCell = r0.createCell(0);
            brandCell.setCellValue("ACMAFER");
            brandCell.setCellStyle(brandStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));
            Cell dateHeaderCell = r0.createCell(8);
            dateHeaderCell.setCellValue("Generado: " + LocalDateTime.now().format(FMT));
            dateHeaderCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 8, 10));
            // Rellenar celdas de la franja azul
            for (int c = 1; c <= 7; c++) {
                Cell fc = r0.createCell(c);
                fc.setCellStyle(brandStyle);
            }

            // Fila 1: Subtítulo "Excelencia en Fundición Industrial"
            Row r1 = sheet.createRow(rowIdx++);
            r1.setHeightInPoints(16);
            Cell subCell = r1.createCell(0);
            subCell.setCellValue("Excelencia en Fundición Industrial");
            subCell.setCellStyle(subStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 10));
            for (int c = 1; c <= 10; c++) {
                Cell fc = r1.createCell(c);
                fc.setCellStyle(subStyle);
            }

            // Fila 2: Franja naranja (acento decorativo, igual que PDF)
            Row r2 = sheet.createRow(rowIdx++);
            r2.setHeightInPoints(4);
            for (int c = 0; c <= 10; c++) {
                Cell ac = r2.createCell(c);
                ac.setCellStyle(accentStyle);
            }

            // Fila 3: Vacía pequeña
            Row r3 = sheet.createRow(rowIdx++);
            r3.setHeightInPoints(8);

            // Fila 4: Título del reporte
            Row r4 = sheet.createRow(rowIdx++);
            r4.setHeightInPoints(22);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("Reporte de Inventario de Productos");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 10));

            // Fila 5: Vacía
            sheet.createRow(rowIdx++).setHeightInPoints(8);

            // ── KPIs ────────────────────────────────────────────────────
            List<Producto> productos = productoRepo.findAll();
            long disponibles = productos.stream().filter(p -> "Disponible".equals(p.getEstado())).count();
            long stockBajoCount = productos.stream().filter(Producto::tieneStockBajo).count();
            BigDecimal valorInventario = productos.stream()
                    .map(p -> p.getPrecioUnitario() != null
                            ? p.getPrecioUnitario().multiply(BigDecimal.valueOf(p.getStockActual()))
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Fila 6: Valores KPI
            Row kpiValRow = sheet.createRow(rowIdx++);
            kpiValRow.setHeightInPoints(36);
            // KPI 1: Total Productos (azul)
            Cell kpi1v = kpiValRow.createCell(0);
            kpi1v.setCellValue(productos.size());
            kpi1v.setCellStyle(kpiValStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2));
            // KPI 2: Disponibles (verde)
            Cell kpi2v = kpiValRow.createCell(3);
            kpi2v.setCellValue(disponibles);
            kpi2v.setCellStyle(kpiGreenValStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 3, 5));
            // KPI 3: Stock Bajo (rojo)
            Cell kpi3v = kpiValRow.createCell(6);
            kpi3v.setCellValue(stockBajoCount);
            kpi3v.setCellStyle(kpiRedValStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 6, 7));
            // KPI 4: Valor Inventario (naranja)
            Cell kpi4v = kpiValRow.createCell(8);
            kpi4v.setCellValue("$" + formatMiles(valorInventario));
            kpi4v.setCellStyle(kpiOrangeValStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 8, 10));
            // Rellenar celdas vacías de la misma fila para el merge
            for (int c : new int[] { 1, 2, 4, 5, 7, 9, 10 }) {
                Cell fc = kpiValRow.createCell(c);
                fc.setCellStyle(kpiValStyle);
            }

            // Fila 7: Etiquetas KPI
            Row kpiLblRow = sheet.createRow(rowIdx++);
            kpiLblRow.setHeightInPoints(18);
            Cell kpi1l = kpiLblRow.createCell(0);
            kpi1l.setCellValue("Total Productos");
            kpi1l.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2));
            Cell kpi2l = kpiLblRow.createCell(3);
            kpi2l.setCellValue("Disponibles");
            kpi2l.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 3, 5));
            Cell kpi3l = kpiLblRow.createCell(6);
            kpi3l.setCellValue("Stock Bajo");
            kpi3l.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 6, 7));
            Cell kpi4l = kpiLblRow.createCell(8);
            kpi4l.setCellValue("Valor Inventario");
            kpi4l.setCellStyle(kpiLabelStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 8, 10));
            for (int c : new int[] { 1, 2, 4, 5, 7, 9, 10 }) {
                Cell fc = kpiLblRow.createCell(c);
                fc.setCellStyle(kpiLabelStyle);
            }

            // Fila 8: Vacía
            sheet.createRow(rowIdx++).setHeightInPoints(10);

            // ── Encabezado de tabla ──────────────────────────────────────
            String[] headers = { "ID", "Código", "Nombre", "Descripción", "Categoría",
                    "Precio Unit.", "Stock", "Stock Mín.", "Estado", "Ventas Tot.", "Fecha Creación" };
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(hdrStyle);
            }

            // ── Filas de datos ───────────────────────────────────────────
            boolean alt = false;
            for (Producto p : productos) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(18);
                boolean stockBajo = p.tieneStockBajo();
                boolean isAlt = alt;

                // ID
                Cell idCell = row.createCell(0);
                idCell.setCellValue(p.getId());
                idCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                // Código (bold azul)
                Cell codigoCell = row.createCell(1);
                codigoCell.setCellValue(p.getCodigo() != null ? p.getCodigo() : "");
                codigoCell.setCellStyle(isAlt ? codeAlt : codeWhite);

                // Nombre
                Cell nombreCell = row.createCell(2);
                nombreCell.setCellValue(p.getNombre());
                nombreCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                // Descripción
                Cell descCell = row.createCell(3);
                descCell.setCellValue(p.getDescripcion() != null ? p.getDescripcion() : "");
                descCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                // Categoría
                Cell catCell = row.createCell(4);
                catCell.setCellValue(p.getCategoria() != null ? p.getCategoria().getNombre() : "");
                catCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                // Precio (alineado a la derecha)
                Cell precioCell = row.createCell(5);
                precioCell.setCellValue(p.getPrecioUnitario() != null ? "$" + formatMiles(p.getPrecioUnitario()) : "-");
                precioCell.setCellStyle(isAlt ? priceAlt : priceWhite);

                // Stock (alerta si stock bajo)
                Cell stockCell = row.createCell(6);
                stockCell.setCellValue(p.getStockActual());
                stockCell.setCellStyle(stockBajo ? stockAlertStyle : (isAlt ? stockNormalAlt : stockNormalWhite));

                // Stock Mínimo
                Cell stockMinCell = row.createCell(7);
                stockMinCell.setCellValue(p.getStockMinimo());
                stockMinCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                // Estado — badge de color
                Cell estadoCell = row.createCell(8);
                estadoCell.setCellValue(p.getEstado() != null ? p.getEstado().toUpperCase() : "-");
                estadoCell.setCellStyle(switch (p.getEstado() != null ? p.getEstado() : "") {
                    case "Disponible" -> badgeGreen;
                    case "Agotado" -> badgeRed;
                    case "Descontinuado" -> badgeGray;
                    default -> isAlt ? cellAlt : cellWhite;
                });

                // Ventas Totales
                Cell ventasCell = row.createCell(9);
                ventasCell.setCellValue(p.getVentasTotales());
                ventasCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                // Fecha Creación
                Cell fechaCell = row.createCell(10);
                fechaCell.setCellValue(p.getFechaCreacion() != null ? p.getFechaCreacion().format(FMT) : "");
                fechaCell.setCellStyle(isAlt ? cellAlt : cellWhite);

                alt = !alt;
            }

            // Fila vacía de separación
            sheet.createRow(rowIdx++).setHeightInPoints(8);

            // ── Pie de documento ─────────────────────────────────────────
            Row footerRow = sheet.createRow(rowIdx);
            footerRow.setHeightInPoints(16);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("Total de registros: " + productos.size()
                    + "   •   Documento generado automáticamente por el sistema ACMAFER");
            footerCell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 10));
            for (int c = 1; c <= 10; c++) {
                Cell fc = footerRow.createCell(c);
                fc.setCellStyle(footerStyle);
            }

            // ── Inmovilizar paneles (fijar encabezado) ───────────────────
            sheet.createFreezePane(0, 9); // fija hasta la fila de encabezado de tabla

            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exportando Excel productos", e);
            throw new RuntimeException("Error exportando Excel: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXCEL: Plantilla de carga masiva — diseño corporativo ACMAFER
    // ═══════════════════════════════════════════════════════════════
    public byte[] generarTemplateExcel() {
        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Plantilla_Productos");
            sheet.setTabColor(new XSSFColor(new byte[] { 27, 43, 75 }, null));

            // ── Paleta ───────────────────────────────────────────────────
            XSSFColor blue = new XSSFColor(new byte[] { (byte) 27, (byte) 43, (byte) 75 }, null);
            XSSFColor orange = new XSSFColor(new byte[] { (byte) 230, (byte) 126, (byte) 34 }, null);
            XSSFColor gray = new XSSFColor(new byte[] { (byte) 120, (byte) 120, (byte) 120 }, null);
            XSSFColor white = new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null);
            XSSFColor softBg = new XSSFColor(new byte[] { (byte) 250, (byte) 250, (byte) 252 }, null);
            XSSFColor inputBg = new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 240 }, null); // amarillo muy
                                                                                                        // suave = celda
                                                                                                        // editable
            XSSFColor borderColor = new XSSFColor(new byte[] { (byte) 228, (byte) 228, (byte) 233 }, null);
            XSSFColor green = new XSSFColor(new byte[] { (byte) 39, (byte) 174, (byte) 96 }, null);
            XSSFColor darkGray = new XSSFColor(new byte[] { (byte) 64, (byte) 64, (byte) 64 }, null);

            // Anchos
            int[] colWidths = { 14 * 256, 30 * 256, 42 * 256, 14 * 256, 16 * 256, 12 * 256, 14 * 256, 14 * 256 };
            for (int i = 0; i < colWidths.length; i++)
                sheet.setColumnWidth(i, colWidths[i]);

            // ── Estilos ───────────────────────────────────────────────────
            // Franja azul — logo
            XSSFCellStyle brandStyle = wb.createCellStyle();
            XSSFFont brandFont = wb.createFont();
            brandFont.setBold(true);
            brandFont.setFontHeightInPoints((short) 18);
            brandFont.setColor(white);
            brandStyle.setFont(brandFont);
            brandStyle.setFillForegroundColor(blue);
            brandStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            brandStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Franja azul — subtítulo
            XSSFCellStyle subStyle = wb.createCellStyle();
            XSSFFont subFont = wb.createFont();
            subFont.setFontHeightInPoints((short) 9);
            subFont.setColor(orange);
            subStyle.setFont(subFont);
            subStyle.setFillForegroundColor(blue);
            subStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Franja azul — fecha
            XSSFCellStyle dateStyle = wb.createCellStyle();
            XSSFFont dateFont = wb.createFont();
            dateFont.setFontHeightInPoints((short) 8);
            dateFont.setColor(orange);
            dateStyle.setFont(dateFont);
            dateStyle.setFillForegroundColor(blue);
            dateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dateStyle.setAlignment(HorizontalAlignment.RIGHT);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Acento naranja
            XSSFCellStyle accentStyle = wb.createCellStyle();
            accentStyle.setFillForegroundColor(orange);
            accentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Título
            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleFont.setColor(blue);
            titleStyle.setFont(titleFont);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Instrucciones
            XSSFCellStyle instrStyle = wb.createCellStyle();
            XSSFFont instrFont = wb.createFont();
            instrFont.setFontHeightInPoints((short) 9);
            instrFont.setColor(gray);
            instrStyle.setFont(instrFont);
            instrStyle.setFillForegroundColor(softBg);
            instrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            instrStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Encabezado obligatorio (asterisco — naranja)
            XSSFCellStyle hdrRequiredStyle = wb.createCellStyle();
            XSSFFont hdrReqFont = wb.createFont();
            hdrReqFont.setBold(true);
            hdrReqFont.setFontHeightInPoints((short) 9);
            hdrReqFont.setColor(white);
            hdrRequiredStyle.setFont(hdrReqFont);
            hdrRequiredStyle.setFillForegroundColor(blue);
            hdrRequiredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hdrRequiredStyle.setAlignment(HorizontalAlignment.CENTER);
            hdrRequiredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            hdrRequiredStyle.setBorderBottom(BorderStyle.MEDIUM);
            hdrRequiredStyle.setBottomBorderColor(orange);
            hdrRequiredStyle.setBorderTop(BorderStyle.THIN);
            hdrRequiredStyle.setTopBorderColor(blue);
            hdrRequiredStyle.setBorderLeft(BorderStyle.THIN);
            hdrRequiredStyle.setLeftBorderColor(blue);
            hdrRequiredStyle.setBorderRight(BorderStyle.THIN);
            hdrRequiredStyle.setRightBorderColor(blue);

            // Encabezado opcional (gris)
            XSSFCellStyle hdrOptStyle = wb.createCellStyle();
            XSSFFont hdrOptFont = wb.createFont();
            hdrOptFont.setBold(true);
            hdrOptFont.setFontHeightInPoints((short) 9);
            hdrOptFont.setColor(white);
            hdrOptStyle.setFont(hdrOptFont);
            XSSFColor darkBlueOpt = new XSSFColor(new byte[] { (byte) 60, (byte) 80, (byte) 110 }, null);
            hdrOptStyle.setFillForegroundColor(darkBlueOpt);
            hdrOptStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hdrOptStyle.setAlignment(HorizontalAlignment.CENTER);
            hdrOptStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            hdrOptStyle.setBorderBottom(BorderStyle.MEDIUM);
            hdrOptStyle.setBottomBorderColor(gray);
            hdrOptStyle.setBorderTop(BorderStyle.THIN);
            hdrOptStyle.setTopBorderColor(darkBlueOpt);
            hdrOptStyle.setBorderLeft(BorderStyle.THIN);
            hdrOptStyle.setLeftBorderColor(darkBlueOpt);
            hdrOptStyle.setBorderRight(BorderStyle.THIN);
            hdrOptStyle.setRightBorderColor(darkBlueOpt);

            // Celda de entrada (fondo amarillo suave = editable)
            XSSFCellStyle inputStyle = wb.createCellStyle();
            XSSFFont inputFont = wb.createFont();
            inputFont.setFontHeightInPoints((short) 9);
            inputFont.setColor(darkGray);
            inputStyle.setFont(inputFont);
            inputStyle.setFillForegroundColor(inputBg);
            inputStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            inputStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            inputStyle.setBorderBottom(BorderStyle.THIN);
            inputStyle.setBottomBorderColor(borderColor);
            inputStyle.setBorderTop(BorderStyle.THIN);
            inputStyle.setTopBorderColor(borderColor);
            inputStyle.setBorderLeft(BorderStyle.THIN);
            inputStyle.setLeftBorderColor(borderColor);
            inputStyle.setBorderRight(BorderStyle.THIN);
            inputStyle.setRightBorderColor(borderColor);

            // Etiqueta de leyenda requerido
            XSSFCellStyle legendReqStyle = wb.createCellStyle();
            XSSFFont lrFont = wb.createFont();
            lrFont.setBold(true);
            lrFont.setFontHeightInPoints((short) 8);
            lrFont.setColor(orange);
            legendReqStyle.setFont(lrFont);
            legendReqStyle.setFillForegroundColor(softBg);
            legendReqStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Etiqueta de leyenda opcional
            XSSFCellStyle legendOptStyle = wb.createCellStyle();
            XSSFFont loFont = wb.createFont();
            loFont.setFontHeightInPoints((short) 8);
            loFont.setColor(gray);
            legendOptStyle.setFont(loFont);
            legendOptStyle.setFillForegroundColor(softBg);
            legendOptStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Pie
            XSSFCellStyle footerStyle = wb.createCellStyle();
            XSSFFont footerFont = wb.createFont();
            footerFont.setItalic(true);
            footerFont.setFontHeightInPoints((short) 8);
            footerFont.setColor(gray);
            footerStyle.setFont(footerFont);
            footerStyle.setFillForegroundColor(softBg);
            footerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            footerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // ── Construcción del encabezado de marca ─────────────────────
            int rowIdx = 0;

            // Fila 0: Franja azul — logo
            Row r0 = sheet.createRow(rowIdx++);
            r0.setHeightInPoints(28);
            Cell brandCell = r0.createCell(0);
            brandCell.setCellValue("ACMAFER");
            brandCell.setCellStyle(brandStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));
            Cell dateCell = r0.createCell(5);
            dateCell.setCellValue("Generado: " + LocalDateTime.now().format(FMT));
            dateCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 5, 7));
            for (int c = 1; c <= 4; c++) {
                Cell fc = r0.createCell(c);
                fc.setCellStyle(brandStyle);
            }
            for (int c = 6; c <= 7; c++) {
                Cell fc = r0.createCell(c);
                fc.setCellStyle(dateStyle);
            }

            // Fila 1: Subtítulo
            Row r1 = sheet.createRow(rowIdx++);
            r1.setHeightInPoints(14);
            Cell subCell = r1.createCell(0);
            subCell.setCellValue("Excelencia en Fundición Industrial");
            subCell.setCellStyle(subStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 7));
            for (int c = 1; c <= 7; c++) {
                Cell fc = r1.createCell(c);
                fc.setCellStyle(subStyle);
            }

            // Fila 2: Franja naranja
            Row r2 = sheet.createRow(rowIdx++);
            r2.setHeightInPoints(4);
            for (int c = 0; c <= 7; c++) {
                Cell ac = r2.createCell(c);
                ac.setCellStyle(accentStyle);
            }

            // Fila 3: Vacía pequeña
            sheet.createRow(rowIdx++).setHeightInPoints(8);

            // Fila 4: Título
            Row r4 = sheet.createRow(rowIdx++);
            r4.setHeightInPoints(22);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("Plantilla de Carga Masiva — Productos");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 7));

            // Fila 5: Instrucciones
            Row r5 = sheet.createRow(rowIdx++);
            r5.setHeightInPoints(16);
            Cell instrCell = r5.createCell(0);
            instrCell.setCellValue(
                    "Complete los campos desde la fila 8 en adelante. Los campos marcados con * son obligatorios. Las celdas en amarillo indican datos editables.");
            instrCell.setCellStyle(instrStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(5, 5, 0, 7));
            for (int c = 1; c <= 7; c++) {
                Cell fc = r5.createCell(c);
                fc.setCellStyle(instrStyle);
            }

            // Fila 6: Leyenda
            Row r6 = sheet.createRow(rowIdx++);
            r6.setHeightInPoints(14);
            Cell legReq = r6.createCell(0);
            legReq.setCellValue("■ Campo obligatorio (*)");
            legReq.setCellStyle(legendReqStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(6, 6, 0, 2));
            Cell legOpt = r6.createCell(3);
            legOpt.setCellValue("■ Campo opcional");
            legOpt.setCellStyle(legendOptStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(6, 6, 3, 5));
            for (int c = 1; c <= 2; c++) {
                Cell fc = r6.createCell(c);
                fc.setCellStyle(legendReqStyle);
            }
            for (int c = 4; c <= 5; c++) {
                Cell fc = r6.createCell(c);
                fc.setCellStyle(legendOptStyle);
            }

            // ── Fila 7: Encabezados de tabla ────────────────────────────
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(22);
            String[] reqCols = { "codigo*", "nombre*", null, "idCategoria*", "precioUnitario*", null, null, null };
            String[] dispCols = { "Código", "Nombre", "Descripción", "ID Categoría", "Precio Unit.", "Stock Actual",
                    "Stock Mín.", "Estado" };
            boolean[] required = { true, true, false, true, true, false, false, false };
            for (int i = 0; i < dispCols.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(dispCols[i] + (required[i] ? " *" : ""));
                c.setCellStyle(required[i] ? hdrRequiredStyle : hdrOptStyle);
            }

            // ── Fila 8: Fila de ejemplo ──────────────────────────────────
            Row exRow = sheet.createRow(rowIdx++);
            exRow.setHeightInPoints(18);
            String[] exVals = { "ACM-999", "Nombre del producto", "Descripción del producto", "1", "99000", "10", "5",
                    "Disponible" };
            for (int i = 0; i < exVals.length; i++) {
                Cell c = exRow.createCell(i);
                c.setCellValue(exVals[i]);
                c.setCellStyle(inputStyle);
            }

            // ── Filas 9–58: Celdas de entrada en blanco (50 filas) ──────
            for (int i = 0; i < 50; i++) {
                Row dataRow = sheet.createRow(rowIdx++);
                dataRow.setHeightInPoints(18);
                for (int c = 0; c < 8; c++) {
                    Cell cell = dataRow.createCell(c);
                    cell.setCellStyle(inputStyle);
                }
            }

            // Separación
            sheet.createRow(rowIdx++).setHeightInPoints(8);

            // ── Pie ──────────────────────────────────────────────────────
            Row footerRow = sheet.createRow(rowIdx);
            footerRow.setHeightInPoints(16);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue(
                    "Plantilla generada automáticamente por el sistema ACMAFER • No modificar la estructura de columnas");
            footerCell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 7));
            for (int c = 1; c <= 7; c++) {
                Cell fc = footerRow.createCell(c);
                fc.setCellStyle(footerStyle);
            }

            // ── Inmovilizar la fila de encabezados ───────────────────────
            sheet.createFreezePane(0, 8);

            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando template: " + e.getMessage());
        }
    }

    // Helper privado: crea estilo de celda de datos con borde uniforme
    private XSSFCellStyle createDataCellStyle(XSSFWorkbook wb, XSSFColor bg, XSSFColor fg, XSSFColor border) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(fg);
        style.setFont(font);
        style.setFillForegroundColor(bg);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(border);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(border);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(border);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(border);
        return style;
    }

     public byte[] generarPdfVentasVendedor(com.acmafer.modulos.usuarios.entidad.Usuario vendedor) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new ReportePageEvent());
            doc.open();
 
            com.itextpdf.text.Font hdrFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
            com.itextpdf.text.Font kpiValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, ACMAFER_BLUE);
            com.itextpdf.text.Font kpiLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
 
            addReportHeader(doc, "Reporte de Ventas — " + vendedor.getNombreCompleto());
 
            // Todos los pedidos del sistema (el vendedor gestiona pedidos de cualquier cliente)
            List<Pedido> todos = pedidoRepo.findAllByOrderByFechaPedidoDesc(PageRequest.of(0, 1000)).getContent();
 
            long pendientes  = todos.stream().filter(p -> "Pendiente".equals(p.getEstado())).count();
            long procesando  = todos.stream().filter(p -> "Procesando".equals(p.getEstado())).count();
            long entregados  = todos.stream().filter(p -> "Entregado".equals(p.getEstado())).count();
            BigDecimal totalVentas = todos.stream()
                    .filter(p -> !"Cancelado".equals(p.getEstado()))
                    .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
 
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(14);
            addKpiCardCell(kpiTable, String.valueOf(pendientes), "Pendientes", kpiValFont, kpiLabelFont, ACMAFER_ORANGE);
            addKpiCardCell(kpiTable, String.valueOf(procesando), "En Proceso", kpiValFont, kpiLabelFont, ACMAFER_BLUE);
            addKpiCardCell(kpiTable, String.valueOf(entregados), "Entregados", kpiValFont, kpiLabelFont, ACMAFER_GREEN);
            addKpiCardCell(kpiTable, "$" + formatMiles(totalVentas), "Total Ventas", kpiValFont, kpiLabelFont, ACMAFER_RED);
            doc.add(kpiTable);
 
            // Tabla de pedidos
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] { 18f, 15f, 30f, 17f, 20f });
            tabla.setHeaderRows(1);
 
            for (String h : new String[] { "N° Pedido", "Estado", "Cliente", "Total", "Fecha" }) {
                PdfPCell cell = new PdfPCell(new Phrase(h, hdrFont));
                cell.setBackgroundColor(ACMAFER_BLUE);
                cell.setPadding(7);
                tabla.addCell(cell);
            }
 
            boolean alt = false;
            for (Pedido p : todos) {
                BaseColor bg = alt ? new BaseColor(245, 248, 252) : BaseColor.WHITE;
                addCell(tabla, p.getNumeroPedido(), cellFont, bg);
                addEstadoPedidoBadge(tabla, p.getEstado(), bg);
                addCell(tabla, p.getUsuario() != null ? p.getUsuario().getNombreCompleto() : "-", cellFont, bg);
                addCell(tabla, p.getTotal() != null ? "$" + formatMiles(p.getTotal()) : "-", cellFont, bg);
                addCell(tabla, p.getFechaPedido() != null ? p.getFechaPedido().format(FMT) : "-", cellFont, bg);
                alt = !alt;
            }
            doc.add(tabla);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(
                    "Total de registros: " + todos.size()
                            + "  •  Generado por " + vendedor.getNombreCompleto()
                            + "  •  Sistema ACMAFER",
                    metaFont));
 
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF de ventas del vendedor", e);
            throw new RuntimeException("Error generando PDF: " + e.getMessage());
        }
    }
 
}