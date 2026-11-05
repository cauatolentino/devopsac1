package com.example.grupo12_praticaatdd.engajamento.aplicacao;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoResumo;
import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import com.example.grupo12_praticaatdd.compartilhado.MesReferencia;
import com.example.grupo12_praticaatdd.engajamento.dominio.ParticipacaoForum;
import com.example.grupo12_praticaatdd.engajamento.persistencia.ParticipacaoForumEntity;
import com.example.grupo12_praticaatdd.engajamento.persistencia.ParticipacaoForumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Contrato publico do modulo {@code engajamento} (RF01 - ranking).
 *
 * Registra a participacao dos alunos no forum e monta o ranking do mes. E o
 * modulo candidato a extracao futura, porque e o que mais cresce em carga: por
 * isso ele so depende do contrato publico do modulo {@code aluno}.
 */
@Service
public class EngajamentoService {

    private static final Comparator<ParticipacaoResumo> MAIOR_ENGAJAMENTO_PRIMEIRO =
            Comparator.comparingInt(ParticipacaoResumo::engajamento).reversed();

    private final ParticipacaoForumRepository participacaoRepository;
    private final AlunoService alunoService;

    public EngajamentoService(ParticipacaoForumRepository participacaoRepository,
                              AlunoService alunoService) {
        this.participacaoRepository = participacaoRepository;
        this.alunoService = alunoService;
    }

    @Transactional
    public ParticipacaoResumo registrar(String nomeAluno, int topicosEscritos, int comentariosAjuda,
                                        String mesReferencia) {
        String mes = MesReferencia.validar(mesReferencia);
        AlunoResumo aluno = alunoService.buscarPorNome(nomeAluno);

        // as validacoes de quantidade negativa vem do dominio
        ParticipacaoForum participacao =
                new ParticipacaoForum(new Aluno(aluno.nome()), topicosEscritos, comentariosAjuda);

        ParticipacaoForumEntity salva = participacaoRepository.save(
                new ParticipacaoForumEntity(aluno.id(), topicosEscritos, comentariosAjuda, mes));
        return resumo(salva, participacao);
    }

    /**
     * Ranking de engajamento do mes, do maior para o menor.
     */
    @Transactional(readOnly = true)
    public List<ParticipacaoResumo> rankingDoMes(String mesReferencia) {
        List<ParticipacaoForumEntity> participacoes =
                participacaoRepository.findByMesReferencia(MesReferencia.validar(mesReferencia));
        Map<Long, Aluno> alunos = alunoService.carregarAlunos(
                participacoes.stream().map(ParticipacaoForumEntity::getAlunoId).toList());

        return participacoes.stream()
                .map(entidade -> resumo(entidade, new ParticipacaoForum(alunos.get(entidade.getAlunoId()),
                        entidade.getTopicosEscritos(), entidade.getComentariosAjuda())))
                .sorted(MAIOR_ENGAJAMENTO_PRIMEIRO)
                .toList();
    }

    /**
     * Totais de participacao de um aluno em todos os meses (usado pelo modulo recomendacao).
     */
    @Transactional(readOnly = true)
    public TotaisDeEngajamento totaisDoAluno(Long alunoId) {
        List<ParticipacaoForumEntity> participacoes = participacaoRepository.findByAlunoId(alunoId);
        return new TotaisDeEngajamento(
                participacoes.stream().mapToInt(ParticipacaoForumEntity::getTopicosEscritos).sum(),
                participacoes.stream().mapToInt(ParticipacaoForumEntity::getComentariosAjuda).sum());
    }

    private ParticipacaoResumo resumo(ParticipacaoForumEntity entidade, ParticipacaoForum participacao) {
        return new ParticipacaoResumo(
                entidade.getId(),
                entidade.getAlunoId(),
                participacao.getAluno().getNome(),
                participacao.getTopicosEscritos(),
                participacao.getComentariosAjuda(),
                participacao.getEngajamento(),
                entidade.getMesReferencia());
    }
}
