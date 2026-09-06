package com.example.grupo12_praticaatdd.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Forum da plataforma. Concentra a regra de negocio da US1: ao encerrar o mes,
 * o aluno de maior engajamento ganha 1 curso.
 *
 * FASE GREEN do TDD: implementacao mais simples que faz os testes passarem.
 */
public class Forum {

    private final List<ParticipacaoForum> participacoes = new ArrayList<>();

    public void registrar(ParticipacaoForum participacao) {
        if (participacao == null) {
            throw new IllegalArgumentException("Participacao e obrigatoria");
        }
        participacoes.add(participacao);
    }

    public Aluno premiarAlunoDoMes() {
        if (participacoes == null || participacoes.isEmpty()) {
            return null;
        }

        ParticipacaoForum vencedora = null;
        for (ParticipacaoForum participacao : participacoes) {
            if (vencedora == null) {
                vencedora = participacao;
            } else if (participacao.getEngajamento() > vencedora.getEngajamento()) {
                vencedora = participacao;
            }
        }

        if (vencedora != null) {
            Aluno premiado = vencedora.getAluno();
            premiado.ganharCurso();
            return premiado;
        }
        return null;
    }

    public List<ParticipacaoForum> getParticipacoes() {
        return Collections.unmodifiableList(participacoes);
    }
}
