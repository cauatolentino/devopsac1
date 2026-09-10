package com.example.grupo12_praticaatdd.dto;

import com.example.grupo12_praticaatdd.entity.AlunoEntity;

/**
 * Representacao de um aluno na API.
 */
public record AlunoDTO(Long id, String nome, int cursosGanhos) {

    public static AlunoDTO de(AlunoEntity entidade) {
        return new AlunoDTO(entidade.getId(), entidade.getNome(), entidade.getCursosGanhos());
    }
}
