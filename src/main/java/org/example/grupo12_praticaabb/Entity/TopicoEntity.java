package org.example.grupo12_praticaabb.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "topicos")
public class TopicoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String titulo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    protected TopicoEntity() {}
    public TopicoEntity(String titulo, UsuarioEntity usuario) { this.titulo = titulo; this.usuario = usuario; }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public UsuarioEntity getUsuario() { return usuario; }
}
