package com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.entrada;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;

/**
 * Porta de entrada (driving port) do modulo premiacao: o caso de uso da US1.
 *
 * Quem dirige o caso de uso (hoje o controller REST; amanha um agendador ou
 * um consumidor de eventos) depende desta interface, nao da implementacao.
 */
public interface EncerrarMes {

    ResultadoPremiacao encerrarMes(String mesReferencia);
}
