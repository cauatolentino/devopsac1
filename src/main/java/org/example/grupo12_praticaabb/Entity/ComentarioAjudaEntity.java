package org.example.grupo12_praticaabb.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comentarios_ajuda")
public class ComentarioAjudaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 500)
    private String conteudo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    protected ComentarioAjudaEntity() {}
    public ComentarioAjudaEntity(String conteudo, UsuarioEntity usuario) { this.conteudo = conteudo; this.usuario = usuario; }
    public Long getId() { return id; }
    public String getConteudo() { return conteudo; }
    public UsuarioEntity getUsuario() { return usuario; }
}
