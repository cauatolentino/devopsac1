package com.example.grupo12_praticaatdd.recomendacao.dominio;

/**
 * O que o Core sabe sobre o aluno e envia para o motor de recomendacao.
 */
public record PerfilDeAprendizagem(String aluno, int cursosGanhos, int topicosEscritos, int comentariosAjuda) {
}
