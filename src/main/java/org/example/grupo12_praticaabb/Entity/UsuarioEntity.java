package org.example.grupo12_praticaabb.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TopicoEntity> topicos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComentarioAjudaEntity> comentariosAjuda = new ArrayList<>();

    protected UsuarioEntity() {}
    public UsuarioEntity(String nome, String email) { this.nome = nome; this.email = email; }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public List<TopicoEntity> getTopicos() { return topicos; }
    public List<ComentarioAjudaEntity> getComentariosAjuda() { return comentariosAjuda; }
}
