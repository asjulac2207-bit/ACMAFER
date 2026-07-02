package com.acmafer.modulos.usuarios.repositorio;


import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.entidad.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumento(String documento);
    long countByRol(Rol rol);
    long countByEstado(String estado);

    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.estado = 'Activo'")
    List<Usuario> findActivosByRol(@Param("rol") Rol rol);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:q IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Usuario> buscar(@Param("q") String q, Pageable pageable);
}
