package com.example.grupo12_praticaatdd.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Forum da plataforma. Concentra a regra de negocio da US1: ao encerrar o mes,
 * o aluno de maior engajamento ganha 1 curso.
 *
 * FASE BLUE do TDD: a busca do vencedor virou uma unica expressao com Stream,
 * eliminando as guardas redundantes e o codigo morto que a etapa GREEN deixou.
 */
public class Forum {

    private static final String PARTICIPACAO_OBRIGATORIA = "Participacao e obrigatoria";

    private static final Comparator<ParticipacaoForum> POR_ENGAJAMENTO =
            Comparator.comparingInt(ParticipacaoForum::getEngajamento);

    private final List<ParticipacaoForum> participacoes = new ArrayList<>();

    /**
     * Registra a participacao de um aluno no forum no mes corrente.
     */
    public void registrar(ParticipacaoForum participacao) {
        if (participacao == null) {
            throw new IllegalArgumentException(PARTICIPACAO_OBRIGATORIA);
        }
        participacoes.add(participacao);
    }

    /**
     * Encerra o mes e premia o aluno de maior engajamento com 1 curso.
     *
     * @return o aluno premiado, ou {@code null} quando nao houve participacao no mes.
     */
    public Aluno premiarAlunoDoMes() {
        return participacoes.stream()
                .max(POR_ENGAJAMENTO)
                .map(this::premiar)
                .orElse(null);
    }

    public List<ParticipacaoForum> getParticipacoes() {
        return List.copyOf(participacoes);
    }

    private Aluno premiar(ParticipacaoForum vencedora) {
        Aluno premiado = vencedora.getAluno();
        premiado.ganharCurso();
        return premiado;
    }
}
