package org.example.grupo12_praticaabb.Repositorio;

import org.example.grupo12_praticaabb.Entity.ComentarioAjudaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioAjudaRepository extends JpaRepository<ComentarioAjudaEntity, Long> {
    long countByUsuarioId(Long usuarioId);
}
