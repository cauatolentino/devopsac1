package com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;

import java.util.Optional;

/**
 * Porta de saida: memoria das premiacoes ja concedidas.
 *
 * Garante a regra "um mes e premiado uma unica vez". O nucleo nao sabe se isso
 * esta numa tabela JPA, em memoria ou em outro servico.
 */
public interface HistoricoDePremiacoes {

    Optional<ResultadoPremiacao> buscar(String mesReferencia);

    void registrar(ResultadoPremiacao premiacao);
}
