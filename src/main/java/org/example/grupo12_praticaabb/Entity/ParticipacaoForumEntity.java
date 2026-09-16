package org.example.grupo12_praticaabb.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "participacao_forum")
public class ParticipacaoForumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeAluno;

    private int quantidadeTopicos;

    private int quantidadeComentarios;

    @ManyToOne
    @JoinColumn(name = "forum_id")
    private ForumEntity forum;

    public ParticipacaoForumEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public int getQuantidadeTopicos() {
        return quantidadeTopicos;
    }

    public void setQuantidadeTopicos(int quantidadeTopicos) {
        this.quantidadeTopicos = quantidadeTopicos;
    }

    public int getQuantidadeComentarios() {
        return quantidadeComentarios;
    }

    public void setQuantidadeComentarios(int quantidadeComentarios) {
        this.quantidadeComentarios = quantidadeComentarios;
    }

    public ForumEntity getForum() {
        return forum;
    }

    public void setForum(ForumEntity forum) {
        this.forum = forum;
    }
}