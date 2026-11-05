package com.example.grupo12_praticaatdd.eventos.aplicacao;

import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxEntity;
import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato publico do modulo eventos: registra um evento de integracao no outbox.
 *
 * {@code Propagation.MANDATORY}: so pode ser chamado DENTRO da transacao da
 * operacao de negocio. Chamado fora de transacao, falha - e isso que garante que
 * nao existe premiacao sem evento nem evento sem premiacao.
 */
@Service
public class RegistroDeEventos {

    private final EventoOutboxRepository repository;

    public RegistroDeEventos(EventoOutboxRepository repository) {
        this.repository = repository;
    }

    /**
     * @return o id unico do evento (usado pelos consumidores para descartar duplicatas)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String registrar(String tipo, String routingKey, String payloadJson) {
        String eventoId = UUID.randomUUID().toString();
        repository.save(new EventoOutboxEntity(eventoId, tipo, routingKey, payloadJson, Instant.now()));
        return eventoId;
    }

    @Transactional(readOnly = true)
    public long pendentes() {
        return repository.countByPublicadoEmIsNull();
    }
}
