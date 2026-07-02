package com.acmafer.modulos.notificaciones.repositorio;


import com.acmafer.modulos.notificaciones.entidad.Notificacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatarioIdAndLeidaFalseOrderByFechaCreacionDesc(Long idUsuario);
    long countByDestinatarioIdAndLeidaFalse(Long idUsuario);
    List<Notificacion> findByDestinatarioIdOrderByFechaCreacionDesc(Long idUsuario, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.destinatario.id = :userId")
    void marcarTodasLeidas(@Param("userId") Long userId);

    void deleteByDestinatarioIdAndTipo(Long idUsuario, String tipo);
}
