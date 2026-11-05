package com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida;

import com.example.grupo12_praticaatdd.engajamento.dominio.ParticipacaoForum;

/**
 * Uma participacao do mes, ja como objeto de dominio, junto com o id do aluno
 * (necessario para creditar o curso depois).
 */
public record ParticipanteDoMes(Long alunoId, ParticipacaoForum participacao) {
}
