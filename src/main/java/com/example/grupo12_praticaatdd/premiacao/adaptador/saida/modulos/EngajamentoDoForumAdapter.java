package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.modulos;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.EngajamentoService;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.ParticipacaoResumo;
import com.example.grupo12_praticaatdd.engajamento.dominio.ParticipacaoForum;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.FonteDeEngajamento;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.ParticipanteDoMes;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Adapter de saida: atende a porta {@link FonteDeEngajamento} chamando, em
 * processo, os contratos publicos dos modulos engajamento e aluno.
 *
 * E o unico ponto do modulo premiacao que conhece os outros modulos. Se o
 * ranking virar um servico, esta classe vira um cliente HTTP.
 */
@Component
public class EngajamentoDoForumAdapter implements FonteDeEngajamento {

    private final EngajamentoService engajamentoService;
    private final AlunoService alunoService;

    public EngajamentoDoForumAdapter(EngajamentoService engajamentoService, AlunoService alunoService) {
        this.engajamentoService = engajamentoService;
        this.alunoService = alunoService;
    }

    @Override
    public List<ParticipanteDoMes> participantesDoMes(String mesReferencia) {
        List<ParticipacaoResumo> ranking = engajamentoService.rankingDoMes(mesReferencia);
        Map<Long, Aluno> alunos = alunoService.carregarAlunos(
                ranking.stream().map(ParticipacaoResumo::alunoId).toList());

        return ranking.stream()
                .map(p -> new ParticipanteDoMes(p.alunoId(), new ParticipacaoForum(
                        alunos.get(p.alunoId()), p.topicosEscritos(), p.comentariosAjuda())))
                .toList();
    }
}
