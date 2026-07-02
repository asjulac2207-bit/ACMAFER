package com.acmafer.modulos.productos.controlador;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.modulos.productos.dto.ProductoDTO;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.CategoriaRepository;
import com.acmafer.modulos.productos.servicio.ProductoService;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepo;

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        var productos = productoService.listar(q, estado, idCategoria,
                PageRequest.of(page, 12, Sort.by("nombre")));
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.getTotalElements());
        model.addAttribute("categorias", categoriaRepo.findByActivoTrue());
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        model.addAttribute("idCategoria", idCategoria);
        model.addAttribute("activePage", "catalogo");
        model.addAttribute("titulo", "Catálogo");
        return "productos/catalogo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.buscarPorId(id));
        model.addAttribute("titulo", "Detalle Producto");
        return "productos/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String nuevoForm(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("categorias", categoriaRepo.findByActivoTrue());
        model.addAttribute("edicion", false);
        model.addAttribute("titulo", "Nuevo Producto");
        return "productos/form";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String crear(@Valid @ModelAttribute("producto") ProductoDTO dto,
            BindingResult br,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findByActivoTrue());
            model.addAttribute("edicion", false);
            return "productos/form";
        }
        try {
            Producto p = productoService.crear(dto, usuario);
            ra.addFlashAttribute("exito", "Producto '" + p.getNombre() + "' creado exitosamente.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos/catalogo";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String editarForm(@PathVariable Long id, Model model) {
        Producto p = productoService.buscarPorId(id);
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setCodigo(p.getCodigo());
        dto.setPrecioUnitario(p.getPrecioUnitario());
        dto.setStockActual(p.getStockActual());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setEstado(p.getEstado());
        dto.setRutaImagen(p.getRutaImagen());

        if (p.getCategoria() != null)
            dto.setIdCategoria(p.getCategoria().getId());
        model.addAttribute("producto", dto);
        model.addAttribute("productoId", id);
        model.addAttribute("categorias", categoriaRepo.findByActivoTrue());
        model.addAttribute("edicion", true);
        model.addAttribute("titulo", "Editar Producto");
        return "productos/form";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String editar(@PathVariable Long id,
            @Valid @ModelAttribute("producto") ProductoDTO dto,
            BindingResult br,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findByActivoTrue());
            model.addAttribute("edicion", true);
            model.addAttribute("productoId", id);
            return "productos/form";
        }
        try {
            productoService.editar(id, dto, usuario);
            ra.addFlashAttribute("exito", "Producto actualizado correctamente.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos/catalogo";
    }

    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String cambiarEstado(@PathVariable Long id,
            @RequestParam String estado,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes ra) {
        try {
            productoService.cambiarEstado(id, estado, usuario);
            ra.addFlashAttribute("exito", "Estado actualizado a: " + estado);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos/catalogo";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public String eliminar(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes ra) {
        try {
            productoService.eliminar(id, usuario);
            ra.addFlashAttribute("exito", "Producto eliminado correctamente.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos/catalogo";
    }
}
