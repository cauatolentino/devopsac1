package com.example.grupo12_praticaatdd.engajamento.aplicacao;

/**
 * Visao de uma participacao que o modulo {@code engajamento} publica para os
 * demais modulos (ex.: {@code premiacao}) e para a propria camada web.
 */
public record ParticipacaoResumo(Long id,
                                 Long alunoId,
                                 String nomeAluno,
                                 int topicosEscritos,
                                 int comentariosAjuda,
                                 int engajamento,
                                 String mesReferencia) {
}
