package com.example.grupo12_praticaatdd.domain;

/**
 * Participacao de um aluno no forum durante o mes corrente.
 *
 * FASE GREEN do TDD: implementacao mais simples que faz os testes passarem.
 */
public class ParticipacaoForum {

    private final Aluno aluno;
    private final int topicosEscritos;
    private final int comentariosAjuda;

    public ParticipacaoForum(Aluno aluno, int topicosEscritos, int comentariosAjuda) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno e obrigatorio");
        }
        if (topicosEscritos < 0) {
            throw new IllegalArgumentException("Quantidade de topicos nao pode ser negativa");
        }
        if (comentariosAjuda < 0) {
            throw new IllegalArgumentException("Quantidade de comentarios nao pode ser negativa");
        }
        this.aluno = aluno;
        this.topicosEscritos = topicosEscritos;
        this.comentariosAjuda = comentariosAjuda;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public int getTopicosEscritos() {
        return topicosEscritos;
    }

    public int getComentariosAjuda() {
        return comentariosAjuda;
    }

    public int getEngajamento() {
        return topicosEscritos + comentariosAjuda;
    }
}
