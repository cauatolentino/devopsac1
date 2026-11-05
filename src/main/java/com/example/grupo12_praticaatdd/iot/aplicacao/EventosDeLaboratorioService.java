package com.example.grupo12_praticaatdd.iot.aplicacao;

import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import com.example.grupo12_praticaatdd.iot.persistencia.EventoLaboratorioEntity;
import com.example.grupo12_praticaatdd.iot.persistencia.EventoLaboratorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Contrato publico do modulo iot (RF06): entrada dos eventos de laboratorio na plataforma.
 *
 * Idempotente: o mesmo eventoId registrado duas vezes conta uma vez so. A
 * checagem previa evita o caso comum (reentrega do QoS 1); a constraint UNIQUE
 * do banco cobre a corrida entre duas entregas simultaneas.
 */
@Service
public class EventosDeLaboratorioService {

    private final EventoLaboratorioRepository repository;

    public EventosDeLaboratorioService(EventoLaboratorioRepository repository) {
        this.repository = repository;
    }

    /**
     * @return {@code true} se o evento era novo; {@code false} se era duplicata (descartada)
     */
    @Transactional
    public boolean registrar(EventoDeLaboratorio evento) {
        if (repository.existsByEventoId(evento.eventoId())) {
            return false;
        }
        repository.save(new EventoLaboratorioEntity(evento, Instant.now()));
        return true;
    }

    @Transactional(readOnly = true)
    public List<EventoDeLaboratorio> eventosDoAluno(String aluno) {
        return repository.findByAlunoOrderByOcorridoEmAsc(aluno).stream()
                .map(EventoLaboratorioEntity::paraDominio)
                .toList();
    }
}
