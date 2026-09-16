package org.example.grupo12_praticaabb.Repositorio;

import org.example.grupo12_praticaabb.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    boolean existsByEmail(String email);
}
