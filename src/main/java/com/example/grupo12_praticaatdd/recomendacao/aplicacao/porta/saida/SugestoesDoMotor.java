package com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida;

import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;

import java.util.List;

/**
 * Resposta do motor: a versao do modelo que gerou as sugestoes e os cursos.
 */
public record SugestoesDoMotor(String versaoModelo, List<CursoRecomendado> cursos) {
}
