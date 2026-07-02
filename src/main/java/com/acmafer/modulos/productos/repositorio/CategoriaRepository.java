package com.acmafer.modulos.productos.repositorio;


import com.acmafer.modulos.productos.entidad.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByActivoTrue();
    boolean existsByNombre(String nombre);
}
