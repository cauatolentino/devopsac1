package org.example.grupo12_praticaabb.Repositorio;

import org.example.grupo12_praticaabb.Entity.TopicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicoRepository extends JpaRepository<TopicoEntity, Long> {
    long countByUsuarioId(Long usuarioId);
}
