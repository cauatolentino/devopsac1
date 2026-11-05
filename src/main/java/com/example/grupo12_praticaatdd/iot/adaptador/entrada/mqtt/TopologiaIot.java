package com.example.grupo12_praticaatdd.iot.adaptador.entrada.mqtt;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Liga os topicos MQTT dos dispositivos a uma fila AMQP duravel do Core.
 *
 * O plugin MQTT do RabbitMQ publica tudo no exchange {@code amq.topic}, trocando
 * "/" por "." no topico. Por isso NAO ha um segundo broker: o mesmo RabbitMQ do
 * Desafio 4 fala MQTT com os dispositivos e AMQP com o Core.
 */
@Configuration
public class TopologiaIot {

    public static final String FILA = "iot.eventos-laboratorio";
    public static final String EXCHANGE_MQTT = "amq.topic";
    public static final String PADRAO_TOPICOS = "lab.*.dispositivo.*.evento";

    @Bean
    Declarables filaDeEventosIot() {
        Queue fila = QueueBuilder.durable(FILA).build();
        return new Declarables(fila,
                new Binding(FILA, Binding.DestinationType.QUEUE, EXCHANGE_MQTT, PADRAO_TOPICOS, null));
    }
}
