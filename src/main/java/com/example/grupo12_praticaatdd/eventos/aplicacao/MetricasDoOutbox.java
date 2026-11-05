package com.example.grupo12_praticaatdd.eventos.aplicacao;

import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Desafio 6 - metrica que VALIDA o ADR-004 (recuperabilidade):
 *
 *   gamificacao_outbox_pendentes  eventos registrados e ainda nao confirmados pelo broker
 *
 * Normal: 0 (o relay esvazia a cada 2 s). Broker fora: cresce, e a premiacao segue
 * funcionando. Broker volta: cai para 0 sem perda. E isso que o alerta OutboxAcumulando
 * observa no Prometheus - o health do Core continua UP, de proposito.
 */
@Component
public class MetricasDoOutbox {

    public MetricasDoOutbox(EventoOutboxRepository repository, MeterRegistry metricas) {
        Gauge.builder("gamificacao.outbox.pendentes", repository, EventoOutboxRepository::countByPublicadoEmIsNull)
                .description("Eventos no outbox ainda nao entregues ao broker")
                .register(metricas);
    }
}
