package com.example.grupo12_praticaatdd.eventos.rabbit;

import com.example.grupo12_praticaatdd.eventos.relay.PublicadorDeMensagens;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Adapter RabbitMQ do relay: mensagem persistente + espera a CONFIRMACAO do broker
 * (publisher confirms). Sem confirmacao, lanca excecao e o evento fica no outbox.
 */
@Component
public class PublicadorRabbitMq implements PublicadorDeMensagens {

    static final long ESPERA_CONFIRMACAO_MS = 5000;

    private final RabbitTemplate rabbitTemplate;

    public PublicadorRabbitMq(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(String routingKey, String eventoId, String tipo, String payloadJson) {
        Message mensagem = MessageBuilder.withBody(payloadJson.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setMessageId(eventoId)
                .setType(tipo)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        rabbitTemplate.invoke(operacoes -> {
            operacoes.send(TopologiaRabbitMq.EXCHANGE, routingKey, mensagem);
            operacoes.waitForConfirmsOrDie(ESPERA_CONFIRMACAO_MS);
            return Boolean.TRUE;
        });
    }
}
