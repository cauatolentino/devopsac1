package org.example.grupo12_praticaabb.DTO;

/**
 * @author Henry Kazumi Tanaka - RA: 248060
 */
public class ParticipacaoRequestDTO {
    private Long idAluno;
    private Long idForum;
    private int quantidadeTopicos;
    private int quantidadeComentarios;

    // Getters e Setters
    public Long getIdAluno() { return idAluno; }
    public void setIdAluno(Long idAluno) { this.idAluno = idAluno; }
    public Long getIdForum() { return idForum; }
    public void setIdForum(Long idForum) { this.idForum = idForum; }
    public int getQuantidadeTopicos() { return quantidadeTopicos; }
    public void setQuantidadeTopicos(int quantidadeTopicos) { this.quantidadeTopicos = quantidadeTopicos; }
    public int getQuantidadeComentarios() { return quantidadeComentarios; }
    public void setQuantidadeComentarios(int quantidadeComentarios) { this.quantidadeComentarios = quantidadeComentarios; }
}