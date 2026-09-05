package com.example.grupo12_praticaatdd.domain;

import java.util.List;

/**
 * Forum da plataforma. Concentra a regra de negocio da US1: ao encerrar o mes,
 * o aluno de maior engajamento ganha 1 curso.
 *
 * FASE RED do TDD: apenas as assinaturas.
 */
public class Forum {

    public void registrar(ParticipacaoForum participacao) {
        // RED - ainda nao implementado
    }

    public Aluno premiarAlunoDoMes() {
        return null;
    }

    public List<ParticipacaoForum> getParticipacoes() {
        return List.of();
    }
}
