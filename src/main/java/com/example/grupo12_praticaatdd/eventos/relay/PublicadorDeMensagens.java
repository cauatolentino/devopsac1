package com.example.grupo12_praticaatdd.eventos.relay;

/**
 * Porta de saida do relay: entrega uma mensagem ao broker.
 *
 * Deve lancar excecao se o broker nao CONFIRMAR o recebimento - assim o evento
 * continua pendente no outbox e e tentado de novo.
 */
public interface PublicadorDeMensagens {

    void publicar(String routingKey, String eventoId, String tipo, String payloadJson);
}
