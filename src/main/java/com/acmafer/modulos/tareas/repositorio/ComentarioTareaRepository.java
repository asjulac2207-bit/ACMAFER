package com.acmafer.modulos.tareas.repositorio;

import com.acmafer.modulos.tareas.entidad.ComentarioTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComentarioTareaRepository extends JpaRepository<ComentarioTarea, Long> {
    List<ComentarioTarea> findByTareaIdOrderByFechaComentarioAsc(Long idTarea);
}
