package com.acmafer.modulos.pedidos.repositorio;

import com.acmafer.modulos.pedidos.entidad.DireccionEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DireccionEntregaRepository extends JpaRepository<DireccionEntrega, Long> {
    List<DireccionEntrega> findByUsuarioId(Long usuarioId);

    Optional<DireccionEntrega> findByUsuarioIdAndEsPrincipalTrue(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}