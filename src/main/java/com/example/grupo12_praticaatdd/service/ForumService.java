package com.example.grupo12_praticaatdd.service;

import com.example.grupo12_praticaatdd.domain.Aluno;
import com.example.grupo12_praticaatdd.domain.Forum;
import com.example.grupo12_praticaatdd.domain.ParticipacaoForum;
import com.example.grupo12_praticaatdd.dto.AlunoDTO;
import com.example.grupo12_praticaatdd.dto.NovaParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.NovoAlunoDTO;
import com.example.grupo12_praticaatdd.dto.ParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.PremiacaoDTO;
import com.example.grupo12_praticaatdd.entity.AlunoEntity;
import com.example.grupo12_praticaatdd.entity.ParticipacaoForumEntity;
import com.example.grupo12_praticaatdd.repository.AlunoRepository;
import com.example.grupo12_praticaatdd.repository.ParticipacaoForumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Orquestra os casos de uso da US1.
 *
 * A regra de negocio em si nao mora aqui: o service carrega os dados do banco,
 * monta o {@link Forum} do pacote de dominio (construido por TDD) e persiste o
 * resultado. Assim a regra continua testavel sem Spring e sem banco.
 */
@Service
public class ForumService {

    private static final Pattern MES_REFERENCIA = Pattern.compile("\\d{4}-\\d{2}");

    private final AlunoRepository alunoRepository;
    private final ParticipacaoForumRepository participacaoRepository;

    public ForumService(AlunoRepository alunoRepository,
                        ParticipacaoForumRepository participacaoRepository) {
        this.alunoRepository = alunoRepository;
        this.participacaoRepository = participacaoRepository;
    }

    @Transactional
    public AlunoDTO cadastrarAluno(NovoAlunoDTO novoAluno) {
        // valida o nome pelas regras do dominio antes de tocar no banco
        Aluno aluno = new Aluno(novoAluno.nome());

        if (alunoRepository.existsByNome(aluno.getNome())) {
            throw new IllegalArgumentException("Ja existe um aluno chamado " + aluno.getNome());
        }
        return AlunoDTO.de(alunoRepository.save(new AlunoEntity(aluno.getNome())));
    }

    @Transactional(readOnly = true)
    public List<AlunoDTO> listarAlunos() {
        return alunoRepository.findAll().stream()
                .map(AlunoDTO::de)
                .toList();
    }

    @Transactional
    public ParticipacaoDTO registrarParticipacao(NovaParticipacaoDTO nova) {
        String mes = validarMesReferencia(nova.mesReferencia());
        AlunoEntity alunoEntity = buscarAluno(nova.nomeAluno());

        // as validacoes de quantidade negativa vem do dominio
        new ParticipacaoForum(alunoEntity.paraDominio(), nova.topicosEscritos(), nova.comentariosAjuda());

        ParticipacaoForumEntity entidade = new ParticipacaoForumEntity(
                alunoEntity, nova.topicosEscritos(), nova.comentariosAjuda(), mes);
        return ParticipacaoDTO.de(participacaoRepository.save(entidade));
    }

    /**
     * Ranking de engajamento do mes, do maior para o menor.
     */
    @Transactional(readOnly = true)
    public List<ParticipacaoDTO> listarEngajamentoDoMes(String mesReferencia) {
        return participacaoRepository.findByMesReferencia(validarMesReferencia(mesReferencia)).stream()
                .map(ParticipacaoDTO::de)
                .sorted(Comparator.comparingInt(ParticipacaoDTO::engajamento).reversed())
                .toList();
    }

    /**
     * Encerra o mes e aplica a regra da US1: o aluno de maior engajamento ganha 1 curso.
     */
    @Transactional
    public PremiacaoDTO encerrarMes(String mesReferencia) {
        String mes = validarMesReferencia(mesReferencia);
        List<ParticipacaoForumEntity> participacoes = participacaoRepository.findByMesReferencia(mes);

        Forum forum = new Forum();
        Map<String, AlunoEntity> entidadesPorNome = new LinkedHashMap<>();
        Map<String, ParticipacaoForum> participacoesPorNome = new LinkedHashMap<>();

        for (ParticipacaoForumEntity entidade : participacoes) {
            AlunoEntity alunoEntity = entidade.getAluno();
            String nome = alunoEntity.getNome();
            entidadesPorNome.put(nome, alunoEntity);

            ParticipacaoForum participacao = new ParticipacaoForum(
                    alunoEntity.paraDominio(), entidade.getTopicosEscritos(), entidade.getComentariosAjuda());
            participacoesPorNome.put(nome, participacao);
            forum.registrar(participacao);
        }

        Aluno premiado = forum.premiarAlunoDoMes();
        if (premiado == null) {
            return PremiacaoDTO.semPremiacao(mes);
        }

        AlunoEntity premiadoEntity = entidadesPorNome.get(premiado.getNome());
        premiadoEntity.setCursosGanhos(premiado.getCursosGanhos());
        alunoRepository.save(premiadoEntity);

        int engajamento = participacoesPorNome.get(premiado.getNome()).getEngajamento();
        return PremiacaoDTO.premiado(mes, premiado.getNome(), engajamento, premiado.getCursosGanhos());
    }

    private AlunoEntity buscarAluno(String nome) {
        return alunoRepository.findByNome(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: " + nome));
    }

    private String validarMesReferencia(String mesReferencia) {
        if (mesReferencia == null || !MES_REFERENCIA.matcher(mesReferencia).matches()) {
            throw new IllegalArgumentException(
                    "Mes de referencia deve estar no formato AAAA-MM. Recebido: " + mesReferencia);
        }
        return mesReferencia;
    }
}
