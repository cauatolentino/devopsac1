package com.example.grupo12_praticaatdd.engajamento.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso a tabela de participacoes no forum. Uso restrito ao modulo {@code engajamento}.
 */
public interface ParticipacaoForumRepository extends JpaRepository<ParticipacaoForumEntity, Long> {

    List<ParticipacaoForumEntity> findByMesReferencia(String mesReferencia);

    List<ParticipacaoForumEntity> findByAlunoId(Long alunoId);
}
