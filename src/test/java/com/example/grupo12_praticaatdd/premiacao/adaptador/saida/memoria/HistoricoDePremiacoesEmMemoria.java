package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.memoria;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.HistoricoDePremiacoes;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter FAKE (em memoria) da porta {@link HistoricoDePremiacoes}.
 *
 * Mini-lab do Desafio 2: e o adapter trocado com o JPA. Fica em src/test porque
 * historico em memoria se perde ao reiniciar e permitiria premiar o mesmo mes
 * de novo - serve para teste, nao para producao.
 */
public class HistoricoDePremiacoesEmMemoria implements HistoricoDePremiacoes {

    private final Map<String, ResultadoPremiacao> porMes = new ConcurrentHashMap<>();

    @Override
    public Optional<ResultadoPremiacao> buscar(String mesReferencia) {
        return Optional.ofNullable(porMes.get(mesReferencia));
    }

    @Override
    public void registrar(ResultadoPremiacao premiacao) {
        if (porMes.putIfAbsent(premiacao.mesReferencia(), premiacao) != null) {
            throw new IllegalStateException("Mes ja premiado: " + premiacao.mesReferencia());
        }
    }
}
