package com.example.grupo12_praticaatdd.engajamento.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Participacao de um aluno no forum em um mes de referencia (formato AAAA-MM).
 *
 * B1 / Desafio 1: o aluno e referenciado apenas pelo id (coluna {@code aluno_id},
 * a mesma da AC1). Antes havia um {@code @ManyToOne} para a entidade do aluno,
 * o que amarrava a persistencia deste modulo a persistencia de outro. Agora o
 * modulo {@code engajamento} conhece o aluno somente pelo contrato publico do
 * modulo {@code aluno}.
 */
@Entity
@Table(name = "participacao_forum")
public class ParticipacaoForumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "topicos_escritos", nullable = false)
    private int topicosEscritos;

    @Column(name = "comentarios_ajuda", nullable = false)
    private int comentariosAjuda;

    @Column(name = "mes_referencia", nullable = false, length = 7)
    private String mesReferencia;

    protected ParticipacaoForumEntity() {
        // exigido pelo JPA
    }

    public ParticipacaoForumEntity(Long alunoId, int topicosEscritos, int comentariosAjuda,
                                   String mesReferencia) {
        this.alunoId = alunoId;
        this.topicosEscritos = topicosEscritos;
        this.comentariosAjuda = comentariosAjuda;
        this.mesReferencia = mesReferencia;
    }

    public Long getId() {
        return id;
    }

    public Long getAlunoId() {
        return alunoId;
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
