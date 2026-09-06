package com.example.grupo12_praticaatdd.domain;

/**
 * Aluno assinante da plataforma de Educacao Continuada Gamificada.
 *
 * FASE GREEN do TDD: implementacao mais simples que faz os testes passarem.
 */
public class Aluno {

    private final String nome;
    private int cursosGanhos;

    public Aluno(String nome) {
        if (nome == null) {
            throw new IllegalArgumentException("Nome do aluno e obrigatorio");
        }
        if (nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do aluno e obrigatorio");
        }
        this.nome = nome;
        this.cursosGanhos = 0;
    }

    public String getNome() {
        return nome;
    }

    public int getCursosGanhos() {
        return cursosGanhos;
    }

    public void ganharCurso() {
        this.cursosGanhos = this.cursosGanhos + 1;
    }
}
