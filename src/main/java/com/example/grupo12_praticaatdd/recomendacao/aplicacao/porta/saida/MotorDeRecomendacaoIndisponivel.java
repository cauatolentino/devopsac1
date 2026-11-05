package com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida;

/**
 * O motor de recomendacao nao respondeu a tempo ou respondeu com erro.
 */
public class MotorDeRecomendacaoIndisponivel extends RuntimeException {

    public MotorDeRecomendacaoIndisponivel(String motivo, Throwable causa) {
        super(motivo, causa);
    }
}
