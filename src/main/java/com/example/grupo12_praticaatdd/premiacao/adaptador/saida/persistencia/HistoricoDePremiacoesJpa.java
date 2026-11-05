package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.persistencia;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.HistoricoDePremiacoes;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter de saida JPA da porta {@link HistoricoDePremiacoes} (o usado em producao).
 */
@Component
public class HistoricoDePremiacoesJpa implements HistoricoDePremiacoes {

    private final PremiacaoMensalRepository repository;

    public HistoricoDePremiacoesJpa(PremiacaoMensalRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ResultadoPremiacao> buscar(String mesReferencia) {
        return repository.findByMesReferencia(mesReferencia)
                .map(e -> new ResultadoPremiacao(e.getMesReferencia(), e.getAlunoPremiado(),
                        e.getEngajamento(), e.getCursosGanhos()));
    }

    @Override
    public void registrar(ResultadoPremiacao premiacao) {
        repository.save(new PremiacaoMensalEntity(premiacao.mesReferencia(), premiacao.alunoPremiado(),
                premiacao.engajamento(), premiacao.cursosGanhos()));
    }
}
