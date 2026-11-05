package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data da tabela premiacao_mensal. Visivel apenas para o adapter JPA.
 */
public interface PremiacaoMensalRepository extends JpaRepository<PremiacaoMensalEntity, Long> {

    Optional<PremiacaoMensalEntity> findByMesReferencia(String mesReferencia);
}
