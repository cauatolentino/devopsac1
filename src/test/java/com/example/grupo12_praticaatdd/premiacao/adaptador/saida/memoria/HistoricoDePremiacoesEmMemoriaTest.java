package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.memoria;

import com.example.grupo12_praticaatdd.premiacao.adaptador.saida.ContratoHistoricoDePremiacoes;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.HistoricoDePremiacoes;

/**
 * Contrato da porta executado contra o adapter em memoria (sem Spring, sem banco).
 */
class HistoricoDePremiacoesEmMemoriaTest extends ContratoHistoricoDePremiacoes {

    private final HistoricoDePremiacoes historico = new HistoricoDePremiacoesEmMemoria();

    @Override
    protected HistoricoDePremiacoes historico() {
        return historico;
    }
}
