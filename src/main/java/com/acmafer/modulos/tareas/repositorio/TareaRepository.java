package com.acmafer.modulos.tareas.repositorio;

import com.acmafer.modulos.tareas.entidad.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    long countByEstado(Tarea.Estado estado);
}
