package com.example.grupo12_praticaatdd.iot.adaptador.entrada.mqtt;

import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import com.example.grupo12_praticaatdd.iot.dominio.TipoEventoLaboratorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Camada anticorrupcao: o que entra do dispositivo e o que vira dominio.
 */
class TradutorDeMensagemIotTest {

    private static final String TOPICO = "lab.lab-redes.dispositivo.leitor-07.evento";
    private final TradutorDeMensagemIot tradutor = new TradutorDeMensagemIot();

    @Test
    @DisplayName("Topico + JSON do dispositivo viram um EventoDeLaboratorio")
    void traduzMensagemValida() {
        EventoDeLaboratorio evento = tradutor.traduzir(TOPICO, json("""
                {"eventoId":"leitor-07-0001","tipo":"conclusao_atividade","aluno":"Carla",
                 "atividade":"Pratica de sub-redes","ocorridoEm":"2026-11-04T13:00:00Z",
                 "dispositivo":"TENTATIVA-DE-SE-PASSAR-POR-OUTRO"}"""));

        assertEquals("leitor-07-0001", evento.eventoId());
        assertEquals("lab-redes", evento.laboratorio());
        assertEquals("leitor-07", evento.dispositivo(), "dispositivo vem do TOPICO, nunca do payload");
        assertEquals(TipoEventoLaboratorio.CONCLUSAO_ATIVIDADE, evento.tipo());
        assertEquals("Pratica de sub-redes", evento.atividade());
        assertEquals(Instant.parse("2026-11-04T13:00:00Z"), evento.ocorridoEm());
    }

    @Test
    @DisplayName("Atividade e opcional (ausente ou null), ex.: evento de presenca")
    void atividadeOpcional() {
        assertNull(tradutor.traduzir(TOPICO, json("""
                {"eventoId":"e1","tipo":"PRESENCA","aluno":"Ana","ocorridoEm":"2026-11-04T13:00:00Z"}""")).atividade());
        assertNull(tradutor.traduzir(TOPICO, json("""
                {"eventoId":"e2","tipo":"PRESENCA","aluno":"Ana","atividade":null,"ocorridoEm":"2026-11-04T13:00:00Z"}""")).atividade());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"lab.redes.evento", "sala.redes.dispositivo.x.evento",
            "lab.redes.sensor.x.evento", "lab.redes.dispositivo.x.status"})
    @DisplayName("Topico fora do padrao e rejeitado")
    void topicoInvalido(String topico) {
        assertThrows(MensagemIotInvalida.class, () -> tradutor.traduzir(topico, json("{}")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "isto nao e json",
            "{\"tipo\":\"PRESENCA\",\"aluno\":\"Ana\",\"ocorridoEm\":\"2026-11-04T13:00:00Z\"}",
            "{\"eventoId\":7,\"tipo\":\"PRESENCA\",\"aluno\":\"Ana\",\"ocorridoEm\":\"2026-11-04T13:00:00Z\"}",
            "{\"eventoId\":\"e1\",\"tipo\":\"TELEPORTE\",\"aluno\":\"Ana\",\"ocorridoEm\":\"2026-11-04T13:00:00Z\"}",
            "{\"eventoId\":\"e1\",\"tipo\":\"PRESENCA\",\"aluno\":\"Ana\",\"ocorridoEm\":\"ontem\"}",
            "{\"eventoId\":\" \",\"tipo\":\"PRESENCA\",\"aluno\":\"Ana\",\"ocorridoEm\":\"2026-11-04T13:00:00Z\"}"})
    @DisplayName("Payload invalido (JSON, campo ausente/tipo errado, enum ou data invalida) e rejeitado")
    void payloadInvalido(String corpo) {
        assertThrows(MensagemIotInvalida.class, () -> tradutor.traduzir(TOPICO, json(corpo)));
    }

    private static byte[] json(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8);
    }
}
