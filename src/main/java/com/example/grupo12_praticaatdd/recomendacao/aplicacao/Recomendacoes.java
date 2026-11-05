package com.example.grupo12_praticaatdd.recomendacao.aplicacao;

import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;

import java.util.List;

/**
 * Resultado entregue ao aluno. {@code degradado = true} indica que a IA nao
 * respondeu e as sugestoes vieram do catalogo padrao do Core.
 */
public record Recomendacoes(String aluno,
                            List<CursoRecomendado> cursos,
                            String origem,
                            boolean degradado,
                            String versaoModelo) {

    public static final String ORIGEM_IA = "ia-recomendacao";
    public static final String ORIGEM_FALLBACK = "catalogo-padrao";
}
