package com.example.grupo12_praticaatdd.recomendacao.dominio;

import java.util.List;

/**
 * Comportamento degradado aceitavel (B1 / Desafio 3): quando a IA nao responde,
 * o aluno recebe sugestoes genericas, iguais para todos, em vez de um erro.
 *
 * A regra mora no Core (e nao no servico Python) porque e justamente o que tem
 * que funcionar quando o servico Python esta fora do ar.
 */
public final class CatalogoPadrao {

    private static final List<CursoRecomendado> CURSOS_INTRODUTORIOS = List.of(
            new CursoRecomendado("Fundamentos de engenharia de software", "sugestao padrao (recomendacao personalizada indisponivel)"),
            new CursoRecomendado("Como fazer boas perguntas", "sugestao padrao (recomendacao personalizada indisponivel)"));

    private CatalogoPadrao() {
    }

    public static List<CursoRecomendado> cursosIntrodutorios() {
        return CURSOS_INTRODUTORIOS;
    }
}
