package com.example.grupo12_praticaatdd.premiacao.aplicacao;

/**
 * Resultado do encerramento do mes publicado pelo modulo {@code premiacao}.
 *
 * {@code alunoPremiado} e nulo quando nao houve participacao no mes.
 */
public record ResultadoPremiacao(String mesReferencia,
                                 String alunoPremiado,
                                 int engajamento,
                                 int cursosGanhos) {

    public static ResultadoPremiacao semPremiacao(String mesReferencia) {
        return new ResultadoPremiacao(mesReferencia, null, 0, 0);
    }

    public boolean houvePremiacao() {
        return alunoPremiado != null;
    }
}
