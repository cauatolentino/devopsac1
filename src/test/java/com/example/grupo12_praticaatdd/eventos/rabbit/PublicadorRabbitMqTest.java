package com.example.grupo12_praticaatdd.eventos.rabbit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Adapter RabbitMQ sem broker real: verifica o que e enviado e que a confirmacao e exigida.
 */
class PublicadorRabbitMqTest {

    private final RabbitTemplate template = mock(RabbitTemplate.class);
    private final RabbitOperations operacoes = mock(RabbitOperations.class);

    @Test
    @DisplayName("Envia mensagem persistente com o eventoId e espera a confirmacao do broker")
    void enviaEsperaConfirmacao() {
        executarCallbackDoInvoke();

        new PublicadorRabbitMq(template).publicar("premiacao.concedida", "evt-1", "PremiacaoConcedida", "{\"a\":1}");

        ArgumentCaptor<Message> mensagem = ArgumentCaptor.forClass(Message.class);
        verify(operacoes).send(eq("gamificacao.eventos"), eq("premiacao.concedida"), mensagem.capture());
        verify(operacoes).waitForConfirmsOrDie(PublicadorRabbitMq.ESPERA_CONFIRMACAO_MS);
        assertEquals("evt-1", mensagem.getValue().getMessageProperties().getMessageId());
        assertEquals("PremiacaoConcedida", mensagem.getValue().getMessageProperties().getType());
        assertEquals(MessageDeliveryMode.PERSISTENT, mensagem.getValue().getMessageProperties().getDeliveryMode());
        assertEquals("{\"a\":1}", new String(mensagem.getValue().getBody(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Sem confirmacao do broker, a falha sobe para o relay (evento fica no outbox)")
    void semConfirmacaoFalha() {
        executarCallbackDoInvoke();
        willThrow(new AmqpException("nack")).given(operacoes).waitForConfirmsOrDie(anyLong());

        assertThrows(AmqpException.class, () -> new PublicadorRabbitMq(template)
                .publicar("premiacao.concedida", "evt-1", "PremiacaoConcedida", "{}"));
    }

    @Test
    @DisplayName("Topologia: exchange topic e duas filas DURAVEIS ligadas a premiacao.concedida")
    void topologia() {
        Declarables declarables = new TopologiaRabbitMq().topologiaDeEventos();

        List<Queue> filas = declarables.getDeclarablesByType(Queue.class);
        List<Binding> ligacoes = declarables.getDeclarablesByType(Binding.class);
        TopicExchange exchange = declarables.getDeclarablesByType(TopicExchange.class).get(0);

        assertEquals("gamificacao.eventos", exchange.getName());
        assertTrue(exchange.isDurable());
        assertEquals(List.of("notificacao.premiacao", "analytics.premiacao"), filas.stream().map(Queue::getName).toList());
        assertTrue(filas.stream().allMatch(Queue::isDurable));
        assertTrue(ligacoes.stream().allMatch(b -> b.getRoutingKey().equals("premiacao.concedida")));
    }

    @SuppressWarnings("unchecked")
    private void executarCallbackDoInvoke() {
        given(template.invoke(any(RabbitOperations.OperationsCallback.class))).willAnswer(chamada ->
                ((RabbitOperations.OperationsCallback<Object>) chamada.getArgument(0)).doInRabbit(operacoes));
    }
}
