package com.example.grupo12_praticaatdd.eventos.relay;

import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxEntity;
import com.example.grupo12_praticaatdd.eventos.outbox.EventoOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Relay do outbox contra o H2, com um broker FALSO que pode estar no ar ou fora.
 */
@DataJpaTest
class RelayDoOutboxTest {

    @Autowired
    private EventoOutboxRepository repository;

    private final List<String> entregues = new ArrayList<>();
    private boolean brokerNoAr = true;

    private final PublicadorDeMensagens brokerFalso = (routingKey, eventoId, tipo, payload) -> {
        if (!brokerNoAr) {
            throw new IllegalStateException("Connection refused");
        }
        entregues.add(eventoId);
    };

    @Test
    @DisplayName("Broker no ar: entrega os pendentes em ordem e marca como publicados")
    void entregaPendentesEmOrdem() {
        evento("e1");
        evento("e2");

        int publicados = new RelayDoOutbox(repository, brokerFalso).publicarPendentes();

        assertEquals(2, publicados);
        assertEquals(List.of("e1", "e2"), entregues);
        assertEquals(0, repository.countByPublicadoEmIsNull());
        assertTrue(repository.findAll().stream().allMatch(e -> e.getPublicadoEm() != null && e.getTentativas() == 1));
    }

    @Test
    @DisplayName("Broker fora: nada se perde - o evento fica pendente com o erro registrado")
    void brokerForaMantemPendente() {
        evento("e1");
        evento("e2");
        brokerNoAr = false;
        RelayDoOutbox relay = new RelayDoOutbox(repository, brokerFalso);

        int publicados = relay.publicarPendentes();

        assertEquals(0, publicados);
        assertEquals(2, repository.countByPublicadoEmIsNull());
        EventoOutboxEntity primeiro = repository.findTop50ByPublicadoEmIsNullOrderByIdAsc().get(0);
        assertEquals(1, primeiro.getTentativas());
        assertEquals("Connection refused", primeiro.getUltimoErro());
        assertNull(primeiro.getPublicadoEm());
        assertEquals(0, repository.findTop50ByPublicadoEmIsNullOrderByIdAsc().get(1).getTentativas(),
                "na primeira falha o lote para, para preservar a ordem");

        brokerNoAr = true;
        assertEquals(2, relay.publicarPendentes(), "quando o broker volta, os pendentes saem");
        assertEquals(List.of("e1", "e2"), entregues);
        assertNull(repository.findAll().get(0).getUltimoErro());
        assertNotNull(repository.findAll().get(0).getPublicadoEm());
    }

    @Test
    @DisplayName("Sem pendentes, o relay nao faz nada")
    void semPendentes() {
        assertEquals(0, new RelayDoOutbox(repository, brokerFalso).publicarPendentes());
        assertTrue(entregues.isEmpty());
    }

    @Test
    @DisplayName("Erro muito longo (ou sem mensagem) e registrado sem estourar a coluna")
    void erroLongoOuNulo() {
        EventoOutboxEntity evento = new EventoOutboxEntity("e1", "T", "rk", "{}", Instant.now());

        evento.registrarFalha("x".repeat(800));
        assertEquals(500, evento.getUltimoErro().length());

        evento.registrarFalha(null);
        assertEquals("null", evento.getUltimoErro());
        assertEquals(2, evento.getTentativas());
    }

    private void evento(String id) {
        repository.save(new EventoOutboxEntity(id, "PremiacaoConcedida", "premiacao.concedida", "{}", Instant.now()));
    }
}
