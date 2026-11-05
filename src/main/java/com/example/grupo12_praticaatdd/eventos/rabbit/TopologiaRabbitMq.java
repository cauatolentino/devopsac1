package com.example.grupo12_praticaatdd.eventos.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia do broker, declarada pelo Core na primeira conexao.
 *
 * As filas sao DURAVEIS e criadas pelo produtor de proposito: se um consumidor
 * nunca subiu (ou esta fora do ar), a mensagem fica esperando na fila dele em vez
 * de ser descartada por falta de destino.
 *
 * <pre>
 *   exchange gamificacao.eventos (topic)
 *     premiacao.concedida -> fila notificacao.premiacao  (servico de notificacao, RF05)
 *     premiacao.concedida -> fila analytics.premiacao    (analytics - consumidor futuro)
 * </pre>
 */
@Configuration
public class TopologiaRabbitMq {

    public static final String EXCHANGE = "gamificacao.eventos";
    public static final String PREMIACAO_CONCEDIDA = "premiacao.concedida";
    public static final String FILA_NOTIFICACAO = "notificacao.premiacao";
    public static final String FILA_ANALYTICS = "analytics.premiacao";

    @Bean
    Declarables topologiaDeEventos() {
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        Queue notificacao = QueueBuilder.durable(FILA_NOTIFICACAO).build();
        Queue analytics = QueueBuilder.durable(FILA_ANALYTICS).build();
        Binding paraNotificacao = BindingBuilder.bind(notificacao).to(exchange).with(PREMIACAO_CONCEDIDA);
        Binding paraAnalytics = BindingBuilder.bind(analytics).to(exchange).with(PREMIACAO_CONCEDIDA);
        return new Declarables(exchange, notificacao, analytics, paraNotificacao, paraAnalytics);
    }
}
