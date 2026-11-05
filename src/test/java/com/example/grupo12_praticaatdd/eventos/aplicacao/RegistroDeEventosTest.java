package com.example.grupo12_praticaatdd.eventos.aplicacao;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * O outbox so aceita eventos DENTRO de uma transacao de negocio (Propagation.MANDATORY).
 * Classe propositalmente sem @Transactional: cada teste controla a sua transacao.
 */
@SpringBootTest
class RegistroDeEventosTest {

    @Autowired
    private RegistroDeEventos registroDeEventos;

    @Autowired
    private TransactionTemplate transacao;

    @Autowired
    private MeterRegistry metricas;

    @Test
    @DisplayName("Fora de uma transacao, registrar evento falha (nao existe evento orfao)")
    void foraDeTransacaoFalha() {
        assertThrows(IllegalTransactionStateException.class,
                () -> registroDeEventos.registrar("T", "rk", "{}"));
    }

    @Test
    @DisplayName("Se a transacao de negocio for desfeita, o evento some junto com ela")
    void rollbackLevaOEventoJunto() {
        long antes = registroDeEventos.pendentes();

        transacao.executeWithoutResult(status -> {
            registroDeEventos.registrar("T", "rk", "{}");
            status.setRollbackOnly();
        });

        assertEquals(antes, registroDeEventos.pendentes());
    }

    @Test
    @DisplayName("Dentro da transacao, o evento fica pendente no outbox com id unico")
    void dentroDaTransacaoFicaPendente() {
        long antes = registroDeEventos.pendentes();

        String id1 = transacao.execute(status -> registroDeEventos.registrar("T", "rk", "{}"));
        String id2 = transacao.execute(status -> registroDeEventos.registrar("T", "rk", "{}"));

        assertEquals(antes + 2, registroDeEventos.pendentes());
        assertEquals(36, id1.length());
        assertEquals(false, id1.equals(id2));
        assertEquals(registroDeEventos.pendentes(), metricas.get("gamificacao.outbox.pendentes").gauge().value(),
                "Desafio 6: a metrica do outbox reflete exatamente os pendentes");
    }
}
