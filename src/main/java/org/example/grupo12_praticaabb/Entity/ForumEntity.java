package org.example.grupo12_praticaabb.Entity;

/**
 * @author Henry Kazumi Tanaka - RA: 248060
 */
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum")
public class ForumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    private List<ParticipacaoForumEntity> participacoes = new ArrayList<>();

    public ForumEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<ParticipacaoForumEntity> getParticipacoes() {
        return participacoes;
    }

    public void setParticipacoes(List<ParticipacaoForumEntity> participacoes) {
        this.participacoes = participacoes;
    }
}