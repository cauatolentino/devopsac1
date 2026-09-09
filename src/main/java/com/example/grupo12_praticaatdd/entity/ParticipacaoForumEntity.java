package com.example.grupo12_praticaatdd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Participacao de um aluno no forum em um mes de referencia (formato AAAA-MM).
 */
@Entity
@Table(name = "participacao_forum")
public class ParticipacaoForumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private AlunoEntity aluno;

    @Column(name = "topicos_escritos", nullable = false)
    private int topicosEscritos;

    @Column(name = "comentarios_ajuda", nullable = false)
    private int comentariosAjuda;

    @Column(name = "mes_referencia", nullable = false, length = 7)
    private String mesReferencia;

    protected ParticipacaoForumEntity() {
        // exigido pelo JPA
    }

    public ParticipacaoForumEntity(AlunoEntity aluno, int topicosEscritos, int comentariosAjuda,
                                   String mesReferencia) {
        this.aluno = aluno;
        this.topicosEscritos = topicosEscritos;
        this.comentariosAjuda = comentariosAjuda;
        this.mesReferencia = mesReferencia;
    }

    public Long getId() {
        return id;
    }

    public AlunoEntity getAluno() {
        return aluno;
    }

    public int getTopicosEscritos() {
        return topicosEscritos;
    }

    public int getComentariosAjuda() {
        return comentariosAjuda;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }
}
