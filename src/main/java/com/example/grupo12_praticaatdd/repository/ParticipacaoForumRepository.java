package com.example.grupo12_praticaatdd.repository;

import com.example.grupo12_praticaatdd.entity.ParticipacaoForumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipacaoForumRepository extends JpaRepository<ParticipacaoForumEntity, Long> {

    List<ParticipacaoForumEntity> findByMesReferencia(String mesReferencia);
}
