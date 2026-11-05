package com.example.grupo12_praticaatdd.recomendacao.aplicacao;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoResumo;
import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.EngajamentoService;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.TotaisDeEngajamento;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CatalogoPadrao;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Caso de uso RF03 - recomendacao inteligente (B1 / Desafio 3).
 *
 * Monta o perfil do aluno com dados do Core e pede sugestoes ao servico Python
 * pela porta {@link MotorDeRecomendacao}. Se a IA falhar, aplica o comportamento
 * degradado (catalogo padrao) e responde normalmente: a indisponibilidade da IA
 * nunca vira erro para o aluno.
 *
 * Nenhum fluxo essencial (cadastro, participacao, ranking, premiacao) chama esta
 * classe - regra verificada pelo ArquiteturaModularTest.
 *
 * Desafio 6 - metricas que VALIDAM o ADR-003 (nao sao so metricas tecnicas):
 *   gamificacao_recomendacao_total{origem=ia-recomendacao|catalogo-padrao}  taxa de degradacao
 *   gamificacao_recomendacao_ia_seconds{resultado=ok|falha}                  latencia real da IA x timeout
 */
@Service
public class RecomendacaoService {

    private static final Logger LOG = LoggerFactory.getLogger(RecomendacaoService.class);

    private final AlunoService alunoService;
    private final EngajamentoService engajamentoService;
    private final MotorDeRecomendacao motor;
    private final MeterRegistry metricas;

    public RecomendacaoService(AlunoService alunoService, EngajamentoService engajamentoService,
                               MotorDeRecomendacao motor, MeterRegistry metricas) {
        this.alunoService = alunoService;
        this.engajamentoService = engajamentoService;
        this.motor = motor;
        this.metricas = metricas;
    }

    public Recomendacoes recomendar(String nomeAluno) {
        AlunoResumo aluno = alunoService.buscarPorNome(nomeAluno);
        TotaisDeEngajamento totais = engajamentoService.totaisDoAluno(aluno.id());
        PerfilDeAprendizagem perfil = new PerfilDeAprendizagem(
                aluno.nome(), aluno.cursosGanhos(), totais.topicosEscritos(), totais.comentariosAjuda());

        Timer.Sample cronometro = Timer.start(metricas);
        try {
            SugestoesDoMotor sugestoes = motor.recomendar(perfil);
            registrar(cronometro, "ok", Recomendacoes.ORIGEM_IA);
            return new Recomendacoes(aluno.nome(), sugestoes.cursos(), Recomendacoes.ORIGEM_IA, false,
                    sugestoes.versaoModelo());
        } catch (MotorDeRecomendacaoIndisponivel falha) {
            registrar(cronometro, "falha", Recomendacoes.ORIGEM_FALLBACK);
            LOG.warn("IA de recomendacao indisponivel, usando catalogo padrao: {}", falha.getMessage());
            return new Recomendacoes(aluno.nome(), CatalogoPadrao.cursosIntrodutorios(),
                    Recomendacoes.ORIGEM_FALLBACK, true, null);
        }
    }

    private void registrar(Timer.Sample cronometro, String resultado, String origem) {
        cronometro.stop(metricas.timer("gamificacao.recomendacao.ia", "resultado", resultado));
        metricas.counter("gamificacao.recomendacao", "origem", origem).increment();
    }
}
