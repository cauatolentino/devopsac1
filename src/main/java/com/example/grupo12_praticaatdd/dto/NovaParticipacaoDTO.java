package com.example.grupo12_praticaatdd.dto;

/**
 * Dados de entrada para registrar a participacao de um aluno no forum.
 */
public record NovaParticipacaoDTO(String nomeAluno,
                                  int topicosEscritos,
                                  int comentariosAjuda,
                                  String mesReferencia) {
}
