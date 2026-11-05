package com.example.grupo12_praticaatdd.iot.adaptador.entrada.mqtt;

import com.example.grupo12_praticaatdd.iot.aplicacao.EventosDeLaboratorioService;
import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adapter de entrada IoT: consome a fila que recebe os topicos MQTT dos dispositivos.
 *
 * O Core nao conhece nenhum dispositivo: so a fila iot.eventos-laboratorio. Se o
 * Core estiver fora do ar, a fila (duravel) acumula e ele processa ao voltar.
 *
 * Mensagem invalida e REJEITADA sem reenfileirar - um dispositivo com defeito nao
 * pode travar a fila reenviando a mesma mensagem para sempre.
 *
 * Desafio 6 - metrica que VALIDA o ADR-005:
 *   gamificacao_iot_eventos_total{resultado=registrado|duplicata|rejeitado}
 */
@Component
public class ConsumidorDeEventosIot {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumidorDeEventosIot.class);

    private final TradutorDeMensagemIot tradutor = new TradutorDeMensagemIot();
    private final EventosDeLaboratorioService service;
    private final MeterRegistry metricas;

    public ConsumidorDeEventosIot(EventosDeLaboratorioService service, MeterRegistry metricas) {
        this.service = service;
        this.metricas = metricas;
    }

    @RabbitListener(queues = TopologiaIot.FILA)
    public void receber(Message mensagem) {
        String topico = mensagem.getMessageProperties().getReceivedRoutingKey();
        EventoDeLaboratorio evento;
        try {
            evento = tradutor.traduzir(topico, mensagem.getBody());
        } catch (MensagemIotInvalida invalida) {
            LOG.warn("IoT: mensagem REJEITADA ({}) topico={}", invalida.getMessage(), topico);
            contar("rejeitado");
            throw new AmqpRejectAndDontRequeueException(invalida.getMessage(), invalida);
        }
        if (service.registrar(evento)) {
            LOG.info("IoT: evento {} {} aluno={} dispositivo={} registrado", evento.eventoId(), evento.tipo(),
                    evento.aluno(), evento.dispositivo());
            contar("registrado");
        } else {
            LOG.info("IoT: DUPLICATA descartada eventoId={} dispositivo={}", evento.eventoId(), evento.dispositivo());
            contar("duplicata");
        }
    }

    private void contar(String resultado) {
        metricas.counter("gamificacao.iot.eventos", "resultado", resultado).increment();
    }
}
