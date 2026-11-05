package com.example.grupo12_praticaatdd.aluno.web;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoResumo;

/**
 * Representacao de um aluno na API.
 */
public record AlunoDTO(Long id, String nome, int cursosGanhos) {

    public static AlunoDTO de(AlunoResumo aluno) {
        return new AlunoDTO(aluno.id(), aluno.nome(), aluno.cursosGanhos());
    }
}
