package com.example.grupo12_praticaatdd.eventos.relay;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Dispara o relay periodicamente. Desligado nos testes automatizados
 * (eventos.relay.habilitado=false no surefire) para nao tentar falar com um broker.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "eventos.relay.habilitado", havingValue = "true", matchIfMissing = true)
public class AgendadorDoRelay {

    private final RelayDoOutbox relay;

    public AgendadorDoRelay(RelayDoOutbox relay) {
        this.relay = relay;
    }

    @Scheduled(fixedDelayString = "${eventos.relay.intervalo-ms:2000}")
    public void executar() {
        relay.publicarPendentes();
    }
}
