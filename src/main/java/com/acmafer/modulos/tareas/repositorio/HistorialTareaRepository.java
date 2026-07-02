package com.acmafer.modulos.tareas.repositorio;

import com.acmafer.modulos.tareas.entidad.HistorialTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialTareaRepository extends JpaRepository<HistorialTarea, Long> {
    List<HistorialTarea> findByTareaIdOrderByFechaCambioDesc(Long idTarea);
}
