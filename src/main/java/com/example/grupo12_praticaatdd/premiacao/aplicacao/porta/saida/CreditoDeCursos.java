package com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida;

/**
 * Porta de saida: grava o novo total de cursos ganhos de um aluno.
 */
public interface CreditoDeCursos {

    void creditar(Long alunoId, int totalDeCursosGanhos);
}
