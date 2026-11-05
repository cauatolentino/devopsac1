package com.example.grupo12_praticaatdd.engajamento.web;

import com.example.grupo12_praticaatdd.engajamento.aplicacao.ParticipacaoResumo;

/**
 * Representacao de uma participacao no forum na API, ja com o engajamento calculado.
 *
 * O contrato JSON e o mesmo da AC1: o id interno do aluno nao e exposto.
 */
public record ParticipacaoDTO(Long id,
                              String nomeAluno,
                              int topicosEscritos,
                              int comentariosAjuda,
                              int engajamento,
                              String mesReferencia) {

    public static ParticipacaoDTO de(ParticipacaoResumo participacao) {
        return new ParticipacaoDTO(
                participacao.id(),
                participacao.nomeAluno(),
                participacao.topicosEscritos(),
                participacao.comentariosAjuda(),
                participacao.engajamento(),
                participacao.mesReferencia());
    }
}
