package org.example.grupo12_praticaabb.Domain;

public class Aluno {

    private String nome;
    private int cursosGanhos;

    public Aluno(String nome) {
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
        cursosGanhos++;
    }
}
