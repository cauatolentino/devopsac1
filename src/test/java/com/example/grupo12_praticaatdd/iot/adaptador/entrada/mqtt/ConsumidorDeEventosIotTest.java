package com.example.grupo12_praticaatdd.iot.adaptador.entrada.mqtt;

import com.example.grupo12_praticaatdd.iot.aplicacao.EventosDeLaboratorioService;
import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConsumidorDeEventosIotTest {

    private static final String CORPO = """
            {"eventoId":"leitor-07-0001","tipo":"PRESENCA","aluno":"Carla","ocorridoEm":"2026-11-04T13:00:00Z"}""";

    private final EventosDeLaboratorioService service = mock(EventosDeLaboratorioService.class);
    private final SimpleMeterRegistry metricas = new SimpleMeterRegistry();
    private final ConsumidorDeEventosIot consumidor = new ConsumidorDeEventosIot(service, metricas);

    @Test
    @DisplayName("Mensagem valida (nova ou duplicata) e entregue ao caso de uso e confirmada")
    void mensagemValida() {
        given(service.registrar(any())).willReturn(true, false);

        consumidor.receber(mensagem("lab.lab-redes.dispositivo.leitor-07.evento", CORPO));
        consumidor.receber(mensagem("lab.lab-redes.dispositivo.leitor-07.evento", CORPO));

        verify(service, times(2)).registrar(any(EventoDeLaboratorio.class));
        assertEquals(1.0, contador("registrado"));
        assertEquals(1.0, contador("duplicata"));
    }

    @Test
    @DisplayName("Mensagem invalida e rejeitada SEM reenfileirar (nao trava a fila)")
    void mensagemInvalidaRejeitada() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumidor.receber(mensagem("lab.lab-redes.dispositivo.leitor-07.evento", "lixo")));
        verify(service, never()).registrar(any());
        assertEquals(1.0, contador("rejeitado"));
    }

    @Test
    @DisplayName("Topologia: fila duravel ligada ao amq.topic (onde o plugin MQTT publica)")
    void topologia() {
        Declarables declarables = new TopologiaIot().filaDeEventosIot();
        Queue fila = declarables.getDeclarablesByType(Queue.class).get(0);
        Binding ligacao = declarables.getDeclarablesByType(Binding.class).get(0);

        assertEquals("iot.eventos-laboratorio", fila.getName());
        assertTrue(fila.isDurable());
        assertEquals("amq.topic", ligacao.getExchange());
        assertEquals("lab.*.dispositivo.*.evento", ligacao.getRoutingKey());
    }

    private double contador(String resultado) {
        return metricas.get("gamificacao.iot.eventos").tag("resultado", resultado).counter().count();
    }

    private static Message mensagem(String routingKey, String corpo) {
        Message mensagem = MessageBuilder.withBody(corpo.getBytes(StandardCharsets.UTF_8)).build();
        mensagem.getMessageProperties().setReceivedRoutingKey(routingKey);
        return mensagem;
    }
}
