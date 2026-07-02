package com.acmafer.modulos.pedidos.servicio;

import com.acmafer.modulos.pedidos.entidad.DireccionEntrega;
import com.acmafer.modulos.pedidos.repositorio.DireccionEntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DireccionEntregaService {

    private final DireccionEntregaRepository repo;

    public boolean usuarioTieneDirecciones(Long usuarioId) {
        return repo.existsByUsuarioId(usuarioId);
    }

    public List<DireccionEntrega> obtenerDirecciones(Long usuarioId) {
        return repo.findByUsuarioId(usuarioId);
    }

    public DireccionEntrega buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
    }

    @Transactional
    public DireccionEntrega guardar(DireccionEntrega direccion) {
        if (!usuarioTieneDirecciones(direccion.getUsuario().getId())) {
            direccion.setEsPrincipal(true);
        }
        return repo.save(direccion);
    }

    @Transactional
    public DireccionEntrega actualizar(Long id, DireccionEntrega datos) {
        DireccionEntrega existente = buscarPorId(id);
        existente.setNombreCompleto(datos.getNombreCompleto());
        existente.setTelefono(datos.getTelefono());
        existente.setDireccion(datos.getDireccion());
        existente.setCiudad(datos.getCiudad());
        existente.setEstado(datos.getEstado());
        existente.setCodigoPostal(datos.getCodigoPostal());
        existente.setReferencias(datos.getReferencias());
        existente.setFechaActualizacion(LocalDateTime.now());
        return repo.save(existente);
    }
}