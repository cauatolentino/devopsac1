package com.example.grupo12_praticaatdd.iot.adaptador.entrada.mqtt;

import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import com.example.grupo12_praticaatdd.iot.dominio.TipoEventoLaboratorio;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Camada anticorrupcao do IoT: traduz a mensagem do dispositivo para o dominio.
 *
 * Topico MQTT {@code lab/<laboratorio>/dispositivo/<id>/evento} chega pelo plugin
 * MQTT do RabbitMQ como routing key {@code lab.<laboratorio>.dispositivo.<id>.evento}.
 *
 * Laboratorio e dispositivo sao lidos do TOPICO, nao do payload: o topico e o que
 * o broker autoriza por ACL (proximo passo de seguranca), entao um dispositivo nao
 * consegue se passar por outro so mudando o JSON.
 */
public class TradutorDeMensagemIot {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public EventoDeLaboratorio traduzir(String routingKey, byte[] corpo) {
        String[] partes = routingKey == null ? new String[0] : routingKey.split("\\.");
        if (partes.length != 5 || !"lab".equals(partes[0]) || !"dispositivo".equals(partes[2])
                || !"evento".equals(partes[4])) {
            throw new MensagemIotInvalida("topico fora do padrao lab/<lab>/dispositivo/<id>/evento: " + routingKey);
        }
        try {
            JsonNode json = JSON.readTree(new String(corpo, StandardCharsets.UTF_8));
            return new EventoDeLaboratorio(
                    texto(json, "eventoId"),
                    partes[1],
                    partes[3],
                    TipoEventoLaboratorio.valueOf(texto(json, "tipo").toUpperCase(Locale.ROOT)),
                    texto(json, "aluno"),
                    json.path("atividade").isMissingNode() || json.path("atividade").isNull()
                            ? null : json.path("atividade").asString(),
                    Instant.parse(texto(json, "ocorridoEm")));
        } catch (JacksonException | IllegalArgumentException | DateTimeParseException erro) {
            throw new MensagemIotInvalida("payload invalido: " + erro.getMessage());
        }
    }

    private static String texto(JsonNode json, String campo) {
        JsonNode valor = json.path(campo);
        if (!valor.isString()) {
            throw new IllegalArgumentException("campo obrigatorio ausente: " + campo);
        }
        return valor.asString();
    }
}
