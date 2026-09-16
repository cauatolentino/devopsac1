package org.example.grupo12_praticaabb.Domain;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Henry Kazumi Tanaka - RA: 248060
 */
public class Forum {

    private final List<ParticipacaoForum> participacoes;

    public Forum() {
        this.participacoes = new ArrayList<>();
    }

    public void registrar(ParticipacaoForum participacao) {
        participacoes.add(participacao);
    }


    public Aluno premiarAluno() {

        if (participacoes.isEmpty()) {
            return null;
        }


        ParticipacaoForum melhorParticipacao = participacoes.get(0);


        for (ParticipacaoForum participacao : participacoes) {
            if (participacao.calcularEngajamento() > melhorParticipacao.calcularEngajamento()) {
                melhorParticipacao = participacao;
            }
        }


        Aluno vencedor = melhorParticipacao.getAluno();


        vencedor.ganharCurso();

        return vencedor;
    }
}