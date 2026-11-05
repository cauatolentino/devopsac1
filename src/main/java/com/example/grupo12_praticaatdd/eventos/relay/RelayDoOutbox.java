package com.example.grupo12_praticaatdd.eventos.relay;

import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxEntity;
import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Le os eventos pendentes do outbox e entrega ao broker, em ordem.
 *
 * Entrega "pelo menos uma vez": se o processo cair entre publicar e marcar como
 * publicado, o evento sai de novo. Por isso cada mensagem leva o eventoId e o
 * consumidor descarta duplicatas.
 *
 * Na primeira falha o lote para (preserva a ordem); o agendador tenta de novo
 * no proximo ciclo. Broker fora do ar = eventos acumulam no outbox, nada se perde.
 */
@Component
public class RelayDoOutbox {

    private static final Logger LOG = LoggerFactory.getLogger(RelayDoOutbox.class);

    private final EventoOutboxRepository repository;
    private final PublicadorDeMensagens publicador;

    public RelayDoOutbox(EventoOutboxRepository repository, PublicadorDeMensagens publicador) {
        this.repository = repository;
        this.publicador = publicador;
    }

    /**
     * @return quantos eventos foram entregues neste ciclo
     */
    @Transactional
    public int publicarPendentes() {
        int publicados = 0;
        for (EventoOutboxEntity evento : repository.findTop50ByPublicadoEmIsNullOrderByIdAsc()) {
            try {
                publicador.publicar(evento.getRoutingKey(), evento.getEventoId(), evento.getTipo(), evento.getPayload());
                evento.marcarPublicado(Instant.now());
                publicados++;
            } catch (RuntimeException falha) {
                evento.registrarFalha(falha.getMessage());
                LOG.warn("Broker indisponivel; evento {} continua no outbox (tentativa {}): {}",
                        evento.getEventoId(), evento.getTentativas(), falha.getMessage());
                break;
            }
        }
        if (publicados > 0) {
            LOG.info("Relay do outbox entregou {} evento(s) ao broker", publicados);
        }
        return publicados;
    }
}
