package com.example.grupo12_praticaatdd.eventos.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Tabela evento_outbox. Uso restrito ao modulo eventos.
 */
public interface EventoOutboxRepository extends JpaRepository<EventoOutboxEntity, Long> {

    List<EventoOutboxEntity> findTop50ByPublicadoEmIsNullOrderByIdAsc();

    long countByPublicadoEmIsNull();
}
