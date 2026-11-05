package com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida;

import java.util.List;

/**
 * Porta de saida: de onde vem as participacoes do mes.
 *
 * Hoje e atendida pelo modulo engajamento (chamada em processo). Se o ranking
 * for extraido para um servico (gatilho do ADR-001), so o adapter muda.
 */
public interface FonteDeEngajamento {

    List<ParticipanteDoMes> participantesDoMes(String mesReferencia);
}
