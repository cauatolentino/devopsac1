package com.example.grupo12_praticaatdd.iot.dominio;

import java.time.Instant;

/**
 * Evento vindo de um dispositivo de laboratorio, ja no vocabulario da plataforma.
 *
 * {@code eventoId} e gerado PELO DISPOSITIVO: e o que permite descartar a
 * duplicata quando o MQTT (QoS 1, "pelo menos uma vez") entrega a mesma
 * mensagem de novo apos uma reconexao.
 */
public record EventoDeLaboratorio(String eventoId,
                                  String laboratorio,
                                  String dispositivo,
                                  TipoEventoLaboratorio tipo,
                                  String aluno,
                                  String atividade,
                                  Instant ocorridoEm) {

    public EventoDeLaboratorio {
        exigir(eventoId, "eventoId");
        exigir(laboratorio, "laboratorio");
        exigir(dispositivo, "dispositivo");
        exigir(aluno, "aluno");
        if (tipo == null) {
            throw new IllegalArgumentException("tipo do evento e obrigatorio");
        }
        if (ocorridoEm == null) {
            throw new IllegalArgumentException("ocorridoEm e obrigatorio");
        }
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " e obrigatorio");
        }
    }
}
