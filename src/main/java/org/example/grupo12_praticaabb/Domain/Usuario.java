package org.example.grupo12_praticaabb.Domain;

public class Usuario {
    private final Long id;
    private final String nome;
    private final int quantidadeTopicos;
    private final int quantidadeComentariosAjuda;

    public Usuario(Long id, String nome, int quantidadeTopicos, int quantidadeComentariosAjuda) {
        if (quantidadeTopicos < 0 || quantidadeComentariosAjuda < 0) {
            throw new IllegalArgumentException("As quantidades não podem ser negativas.");
        }
        this.id = id;
        this.nome = nome;
        this.quantidadeTopicos = quantidadeTopicos;
        this.quantidadeComentariosAjuda = quantidadeComentariosAjuda;
    }

    public int calcularEngajamento() {
        return quantidadeTopicos + quantidadeComentariosAjuda;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public int getQuantidadeTopicos() { return quantidadeTopicos; }
    public int getQuantidadeComentariosAjuda() { return quantidadeComentariosAjuda; }
}
