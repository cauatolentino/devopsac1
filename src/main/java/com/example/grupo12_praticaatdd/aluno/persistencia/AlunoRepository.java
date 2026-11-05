package com.example.grupo12_praticaatdd.aluno.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso a tabela de alunos. Uso restrito ao modulo {@code aluno}.
 */
public interface AlunoRepository extends JpaRepository<AlunoEntity, Long> {

    Optional<AlunoEntity> findByNome(String nome);

    boolean existsByNome(String nome);
}
