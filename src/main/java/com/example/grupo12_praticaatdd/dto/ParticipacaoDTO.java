package com.example.grupo12_praticaatdd.dto;

import com.example.grupo12_praticaatdd.entity.ParticipacaoForumEntity;

/**
 * Representacao de uma participacao no forum na API, ja com o engajamento calculado.
 */
public record ParticipacaoDTO(Long id,
                              String nomeAluno,
                              int topicosEscritos,
                              int comentariosAjuda,
                              int engajamento,
                              String mesReferencia) {

    public static ParticipacaoDTO de(ParticipacaoForumEntity entidade) {
        return new ParticipacaoDTO(
                entidade.getId(),
                entidade.getAluno().getNome(),
                entidade.getTopicosEscritos(),
                entidade.getComentariosAjuda(),
                entidade.getTopicosEscritos() + entidade.getComentariosAjuda(),
                entidade.getMesReferencia());
    }
}
