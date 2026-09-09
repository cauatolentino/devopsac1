package com.example.grupo12_praticaatdd.repository;

import com.example.grupo12_praticaatdd.entity.AlunoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<AlunoEntity, Long> {

    Optional<AlunoEntity> findByNome(String nome);

    boolean existsByNome(String nome);
}
