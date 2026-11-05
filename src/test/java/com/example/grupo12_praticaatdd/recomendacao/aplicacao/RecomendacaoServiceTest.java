package com.example.grupo12_praticaatdd.recomendacao.aplicacao;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.compartilhado.RecursoNaoEncontradoException;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.EngajamentoService;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.entrada.EncerrarMes;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CatalogoPadrao;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Testes de integracao do caso de uso RF03 com o motor de IA simulado.
 */
@SpringBootTest
@Transactional
class RecomendacaoServiceTest {

    @Autowired
    private RecomendacaoService recomendacaoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private EngajamentoService engajamentoService;

    @Autowired
    private EncerrarMes encerrarMes;

    @Autowired
    private MeterRegistry metricas;

    @MockitoBean
    private MotorDeRecomendacao motor;

    @Test
    @DisplayName("Envia ao motor o perfil com os totais de todos os meses e os cursos ganhos")
    void enviaPerfilCompletoParaAIa() {
        alunoService.cadastrar("Carla");
        engajamentoService.registrar("Carla", 12, 8, "2026-09");
        engajamentoService.registrar("Carla", 3, 1, "2026-10");
        encerrarMes.encerrarMes("2026-09");
        given(motor.recomendar(any())).willReturn(new SugestoesDoMotor("heuristico-v1",
                List.of(new CursoRecomendado("Escrita tecnica", "perfil autor"))));

        Recomendacoes recomendacoes = recomendacaoService.recomendar("Carla");

        verify(motor).recomendar(new PerfilDeAprendizagem("Carla", 1, 15, 9));
        assertFalse(recomendacoes.degradado());
        assertEquals(Recomendacoes.ORIGEM_IA, recomendacoes.origem());
        assertEquals("heuristico-v1", recomendacoes.versaoModelo());
        assertEquals("Escrita tecnica", recomendacoes.cursos().get(0).titulo());
    }

    @Test
    @DisplayName("IA indisponivel: responde com o catalogo padrao e marca degradado, sem erro")
    void iaIndisponivelUsaCatalogoPadrao() {
        alunoService.cadastrar("Ana");
        double fallbacksAntes = contador(Recomendacoes.ORIGEM_FALLBACK);
        given(motor.recomendar(any()))
                .willThrow(new MotorDeRecomendacaoIndisponivel("conexao recusada", null));

        Recomendacoes recomendacoes = recomendacaoService.recomendar("Ana");

        assertTrue(recomendacoes.degradado());
        assertEquals(Recomendacoes.ORIGEM_FALLBACK, recomendacoes.origem());
        assertEquals(CatalogoPadrao.cursosIntrodutorios(), recomendacoes.cursos());
        assertNull(recomendacoes.versaoModelo());
        assertEquals(fallbacksAntes + 1, contador(Recomendacoes.ORIGEM_FALLBACK),
                "Desafio 6: cada degradacao conta na metrica gamificacao_recomendacao_total");
        assertTrue(metricas.get("gamificacao.recomendacao.ia").tag("resultado", "falha").timer().count() >= 1,
                "a latencia das chamadas que falharam tambem e medida");
    }

    private double contador(String origem) {
        var contador = metricas.find("gamificacao.recomendacao").tag("origem", origem).counter();
        return contador == null ? 0 : contador.count();
    }

    @Test
    @DisplayName("Aluno inexistente continua sendo 404, com ou sem IA")
    void alunoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> recomendacaoService.recomendar("Fulano"));
    }
}
