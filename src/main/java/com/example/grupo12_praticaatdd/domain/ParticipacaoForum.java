package com.example.grupo12_praticaatdd.domain;

/**
 * Participacao de um aluno no forum durante o mes corrente.
 *
 * FASE BLUE do TDD: as duas validacoes de quantidade negativa foram unificadas
 * e as mensagens viraram constantes.
 */
public class ParticipacaoForum {

    private static final String ALUNO_OBRIGATORIO = "Aluno e obrigatorio";
    private static final String QUANTIDADE_NEGATIVA =
            "Quantidade de topicos e de comentarios nao pode ser negativa";

    private final Aluno aluno;
    private final int topicosEscritos;
    private final int comentariosAjuda;

    public ParticipacaoForum(Aluno aluno, int topicosEscritos, int comentariosAjuda) {
        if (aluno == null) {
            throw new IllegalArgumentException(ALUNO_OBRIGATORIO);
        }
        if (topicosEscritos < 0 || comentariosAjuda < 0) {
            throw new IllegalArgumentException(QUANTIDADE_NEGATIVA);
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

    /**
     * Engajamento do aluno no mes: topicos escritos + comentarios de ajuda.
     */
    public int getEngajamento() {
        return topicosEscritos + comentariosAjuda;
    }
}
