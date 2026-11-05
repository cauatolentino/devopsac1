package com.example.grupo12_praticaatdd.recomendacao.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.Recomendacoes;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;

import java.util.List;

/**
 * Representacao das recomendacoes na API.
 */
public record RecomendacoesDTO(String aluno,
                               List<CursoRecomendado> cursos,
                               String origem,
                               boolean degradado,
                               String versaoModelo) {

    public static RecomendacoesDTO de(Recomendacoes recomendacoes) {
        return new RecomendacoesDTO(recomendacoes.aluno(), recomendacoes.cursos(), recomendacoes.origem(),
                recomendacoes.degradado(), recomendacoes.versaoModelo());
    }
}
