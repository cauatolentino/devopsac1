package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.eventos;

import com.example.grupo12_praticaatdd.eventos.aplicacao.RegistroDeEventos;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.EventosDePremiacao;
import org.springframework.stereotype.Component;

/**
 * Adapter de saida: grava o evento "premiacao.concedida" no outbox, dentro da
 * transacao do encerramento do mes. Quem entrega ao RabbitMQ e o relay.
 */
@Component
public class EventosDePremiacaoNoOutbox implements EventosDePremiacao {

    static final String TIPO = "PremiacaoConcedida";
    static final String ROUTING_KEY = "premiacao.concedida";

    private final RegistroDeEventos registroDeEventos;

    public EventosDePremiacaoNoOutbox(RegistroDeEventos registroDeEventos) {
        this.registroDeEventos = registroDeEventos;
    }

    @Override
    public void premiacaoConcedida(Long alunoId, ResultadoPremiacao premiacao) {
        registroDeEventos.registrar(TIPO, ROUTING_KEY, json(alunoId, premiacao));
    }

    static String json(Long alunoId, ResultadoPremiacao premiacao) {
        return "{\"tipo\":\"" + TIPO + "\""
                + ",\"alunoId\":" + alunoId
                + ",\"aluno\":\"" + escapar(premiacao.alunoPremiado()) + "\""
                + ",\"mesReferencia\":\"" + escapar(premiacao.mesReferencia()) + "\""
                + ",\"engajamento\":" + premiacao.engajamento()
                + ",\"cursosGanhos\":" + premiacao.cursosGanhos() + "}";
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
