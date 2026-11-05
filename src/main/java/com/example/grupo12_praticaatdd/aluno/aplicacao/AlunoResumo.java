package com.example.grupo12_praticaatdd.aluno.aplicacao;

import com.example.grupo12_praticaatdd.aluno.persistencia.AlunoEntity;

/**
 * Visao de um aluno que o modulo {@code aluno} publica para os demais modulos
 * e para a propria camada web. Nenhum modulo externo enxerga a entidade JPA.
 */
public record AlunoResumo(Long id, String nome, int cursosGanhos) {

    static AlunoResumo de(AlunoEntity entidade) {
        return new AlunoResumo(entidade.getId(), entidade.getNome(), entidade.getCursosGanhos());
    }
}
