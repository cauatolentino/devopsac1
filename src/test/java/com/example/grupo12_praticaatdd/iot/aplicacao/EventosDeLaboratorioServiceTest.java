package com.example.grupo12_praticaatdd.iot.aplicacao;

import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import com.example.grupo12_praticaatdd.iot.dominio.TipoEventoLaboratorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EventosDeLaboratorioServiceTest {

    @Autowired
    private EventosDeLaboratorioService service;

    @Test
    @DisplayName("Mesmo eventoId entregue duas vezes (reentrega do QoS 1) e registrado uma vez")
    void duplicataDescartada() {
        EventoDeLaboratorio evento = evento("leitor-07-0001", TipoEventoLaboratorio.PRESENCA, "2026-11-04T13:00:00Z");

        assertTrue(service.registrar(evento));
        assertFalse(service.registrar(evento));

        assertEquals(1, service.eventosDoAluno("Carla").size());
    }

    @Test
    @DisplayName("Eventos do aluno voltam em ordem cronologica, mesmo chegando fora de ordem")
    void ordemCronologica() {
        service.registrar(evento("e3", TipoEventoLaboratorio.CONCLUSAO_ATIVIDADE, "2026-11-04T15:00:00Z"));
        service.registrar(evento("e1", TipoEventoLaboratorio.PRESENCA, "2026-11-04T13:00:00Z"));
        service.registrar(evento("e2", TipoEventoLaboratorio.INICIO_ATIVIDADE, "2026-11-04T14:00:00Z"));

        List<String> ids = service.eventosDoAluno("Carla").stream().map(EventoDeLaboratorio::eventoId).toList();

        assertEquals(List.of("e1", "e2", "e3"), ids);
    }

    private static EventoDeLaboratorio evento(String id, TipoEventoLaboratorio tipo, String quando) {
        return new EventoDeLaboratorio(id, "lab-redes", "leitor-07", tipo, "Carla", "Pratica de sub-redes",
                Instant.parse(quando));
    }
}
