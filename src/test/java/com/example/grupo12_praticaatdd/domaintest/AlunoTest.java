package com.example.grupo12_praticaatdd.domaintest;

import com.example.grupo12_praticaatdd.domain.Aluno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de unidade do Aluno.
 *
 * Apoiam o cenario 1 do BDD (autor: Caua Tolentino), que exige que o aluno
 * premiado tenha 1 curso ganho ao final do mes.
 */
class AlunoTest {

    @Test
    @DisplayName("Aluno recem criado tem nome e nenhum curso ganho")
    void deveCriarAlunoSemCursosGanhos() {
        Aluno ana = new Aluno("Ana");

        assertEquals("Ana", ana.getNome());
        assertEquals(0, ana.getCursosGanhos());
    }

    @Test
    @DisplayName("Ganhar um curso incrementa o total de cursos do aluno")
    void deveIncrementarCursosGanhos() {
        Aluno ana = new Aluno("Ana");

        ana.ganharCurso();
        ana.ganharCurso();

        assertEquals(2, ana.getCursosGanhos());
    }

    @Test
    @DisplayName("Nao deve criar aluno com nome nulo")
    void naoDeveCriarAlunoComNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Aluno(null));
    }

    @Test
    @DisplayName("Nao deve criar aluno com nome em branco")
    void naoDeveCriarAlunoComNomeEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> new Aluno("   "));
    }
}
