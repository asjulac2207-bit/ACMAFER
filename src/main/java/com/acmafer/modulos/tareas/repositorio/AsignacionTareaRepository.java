package com.acmafer.modulos.tareas.repositorio;

import com.acmafer.modulos.tareas.entidad.AsignacionTarea;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsignacionTareaRepository extends JpaRepository<AsignacionTarea, Long> {
    List<AsignacionTarea> findByEmpleadoId(Long idEmpleado);

    List<AsignacionTarea> findByTareaId(Long idTarea);

    @Query("SELECT a FROM AsignacionTarea a WHERE a.empleado.id = :id AND a.tarea.estado <> 'COMPLETADA'")
    List<AsignacionTarea> findPendientesByEmpleado(@Param("id") Long id);

    @Query("SELECT COUNT(a) FROM AsignacionTarea a WHERE a.empleado.id = :id AND a.tarea.estado = 'COMPLETADA'")
    long countCompletadasByEmpleado(@Param("id") Long id);

    @Query("SELECT COUNT(a) FROM AsignacionTarea a WHERE a.empleado.id = :id")
    long countTotalByEmpleado(@Param("id") Long id);

    void deleteByEmpleadoId(Long idEmpleado);
}