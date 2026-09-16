package org.example.grupo12_praticaabb.Domain;

public class ParticipacaoForum {

    private Aluno aluno;
    private int quantidadeTopicos;
    private int quantidadeComentarios;

    public ParticipacaoForum(
            Aluno aluno,
            int quantidadeTopicos,
            int quantidadeComentarios) {

        this.aluno = aluno;
        this.quantidadeTopicos = quantidadeTopicos;
        this.quantidadeComentarios = quantidadeComentarios;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public int getQuantidadeTopicos() {
        return quantidadeTopicos;
    }

    public int getQuantidadeComentarios() {
        return quantidadeComentarios;
    }

    public int calcularEngajamento() {
        return quantidadeTopicos + quantidadeComentarios;
    }
}
