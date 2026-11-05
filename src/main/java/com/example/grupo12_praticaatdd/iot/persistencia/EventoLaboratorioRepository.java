package com.example.grupo12_praticaatdd.iot.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Tabela evento_laboratorio. Uso restrito ao modulo iot.
 */
public interface EventoLaboratorioRepository extends JpaRepository<EventoLaboratorioEntity, Long> {

    boolean existsByEventoId(String eventoId);

    List<EventoLaboratorioEntity> findByAlunoOrderByOcorridoEmAsc(String aluno);
}
