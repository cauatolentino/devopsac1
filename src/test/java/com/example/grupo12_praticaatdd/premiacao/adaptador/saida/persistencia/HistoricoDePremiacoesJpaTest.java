package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.persistencia;

import com.example.grupo12_praticaatdd.premiacao.adaptador.saida.ContratoHistoricoDePremiacoes;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.HistoricoDePremiacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Contrato da porta executado contra o adapter JPA (H2 em memoria).
 * A unicidade do mes e garantida pela constraint UNIQUE da tabela.
 */
@DataJpaTest
@Import(HistoricoDePremiacoesJpa.class)
class HistoricoDePremiacoesJpaTest extends ContratoHistoricoDePremiacoes {

    @Autowired
    private HistoricoDePremiacoesJpa historico;

    @Override
    protected HistoricoDePremiacoes historico() {
        return historico;
    }
}
