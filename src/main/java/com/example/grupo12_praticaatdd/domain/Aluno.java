package com.example.grupo12_praticaatdd.domain;

/**
 * Aluno assinante da plataforma de Educacao Continuada Gamificada.
 *
 * FASE BLUE do TDD: as duas validacoes duplicadas do GREEN viraram uma unica
 * condicao e a mensagem de erro virou constante.
 */
public class Aluno {

    private static final String NOME_OBRIGATORIO = "Nome do aluno e obrigatorio";

    private final String nome;
    private int cursosGanhos;

    public Aluno(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(NOME_OBRIGATORIO);
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    public int getCursosGanhos() {
        return cursosGanhos;
    }

    /**
     * Credita um curso ao aluno como premiacao da gamificacao.
     */
    public void ganharCurso() {
        cursosGanhos++;
    }
}
