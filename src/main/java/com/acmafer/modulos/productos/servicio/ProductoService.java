package com.acmafer.modulos.productos.servicio;

import com.acmafer.comun.excepcion.BusinessException;
import com.acmafer.modulos.productos.dto.ProductoDTO;
import com.acmafer.modulos.productos.entidad.Categoria;
import com.acmafer.modulos.productos.entidad.HistorialProducto;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.CategoriaRepository;
import com.acmafer.modulos.productos.repositorio.HistorialProductoRepository;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final HistorialProductoRepository historialRepo;

    @Transactional
    public Producto crear(ProductoDTO dto, Usuario responsable) {
        if (dto.getCodigo() != null && !dto.getCodigo().isBlank()
                && productoRepo.findByCodigo(dto.getCodigo()).isPresent())
            throw new BusinessException("El código de producto ya existe: " + dto.getCodigo());

        Categoria cat = categoriaRepo.findById(dto.getIdCategoria())
                .orElseThrow(() -> new BusinessException("Categoría no encontrada"));

        Producto p = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .codigo(dto.getCodigo())
                .rutaImagen(dto.getRutaImagen())

                .stockActual(dto.getStockActual() != null ? dto.getStockActual() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5)
                .estado(dto.getStockActual() != null && dto.getStockActual() > 0 ? "Disponible" : "Agotado")
                .precioUnitario(dto.getPrecioUnitario())
                .categoria(cat)
                .build();

        Producto guardado = productoRepo.save(p);
        log.info("Producto creado: {} por {}", guardado.getCodigo(), responsable.getEmail());
        return guardado;
    }

    @Transactional
    public Producto editar(Long id, ProductoDTO dto, Usuario responsable) {
        Producto p = buscarPorId(id);

        if (dto.getCodigo() != null && !dto.getCodigo().isBlank()) {
            productoRepo.findByCodigo(dto.getCodigo()).ifPresent(existing -> {
                if (!existing.getId().equals(id))
                    throw new BusinessException("El código ya está en uso por otro producto");
            });
        }

        Categoria cat = categoriaRepo.findById(dto.getIdCategoria())
                .orElseThrow(() -> new BusinessException("Categoría no encontrada"));

        registrarCambio(p, "nombre", p.getNombre(), dto.getNombre(), responsable);
        registrarCambio(p, "precioUnitario",
                p.getPrecioUnitario() != null ? p.getPrecioUnitario().toPlainString() : null,
                dto.getPrecioUnitario() != null ? dto.getPrecioUnitario().toPlainString() : null,
                responsable);

        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setRutaImagen(dto.getRutaImagen());

        p.setCodigo(dto.getCodigo());
        p.setStockActual(dto.getStockActual());
        if (dto.getStockMinimo() != null)
            p.setStockMinimo(dto.getStockMinimo());
        p.setPrecioUnitario(dto.getPrecioUnitario());
        if (dto.getEstado() != null)
            p.setEstado(dto.getEstado());
        p.setCategoria(cat);
        p.setFechaActualizacion(LocalDateTime.now());

        Producto guardado = productoRepo.save(p);
        log.info("Producto editado: {} por {}", guardado.getCodigo(), responsable.getEmail());
        return guardado;
    }

    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado, Usuario responsable) {
        Producto p = buscarPorId(id);
        if ("Descontinuado".equals(nuevoEstado) && p.getStockActual() > 0)
            throw new BusinessException(
                    "No se puede descontinuar un producto con stock disponible (" + p.getStockActual() + " unidades)");

        registrarCambio(p, "estado", p.getEstado(), nuevoEstado, responsable);
        p.setEstado(nuevoEstado);
        p.setFechaActualizacion(LocalDateTime.now());
        productoRepo.save(p);
    }

    public Page<Producto> listar(String q, String estado, Long idCategoria, Pageable pageable) {
        return productoRepo.filtrar(
                (q != null && !q.isBlank()) ? q : null,
                (estado != null && !estado.isBlank()) ? estado : null,
                idCategoria,
                pageable);
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado: " + id));
    }

    public List<Producto> topVendidos(int n) {
        return productoRepo.findTopVendidos(PageRequest.of(0, n));
    }

    public List<Producto> productosConStockBajo() {
        return productoRepo.findConStockBajo();
    }

    public List<HistorialProducto> historial(Long idProducto) {
        return historialRepo.findByProductoIdOrderByFechaDesc(idProducto);
    }

    private void registrarCambio(Producto p, String campo, String anterior, String nuevo, Usuario u) {
        if (anterior == null && nuevo == null)
            return;
        if (anterior != null && anterior.equals(nuevo))
            return;
        historialRepo.save(HistorialProducto.builder()
                .producto(p).campoModificado(campo)
                .valorAnterior(anterior).valorNuevo(nuevo)
                .modificadoPor(u).build());
    }

    @Transactional
    public void eliminar(Long id, Usuario responsable) {
        Producto p = buscarPorId(id);
        log.info("Producto eliminado: {} por {}", p.getCodigo(), responsable.getEmail());
        productoRepo.deleteById(id);
    }
}
