package com.example.grupo12_praticaatdd.premiacao.aplicacao;

import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import com.example.grupo12_praticaatdd.engajamento.dominio.ParticipacaoForum;
import com.example.grupo12_praticaatdd.premiacao.adaptador.saida.memoria.HistoricoDePremiacoesEmMemoria;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.CreditoDeCursos;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.EventosDePremiacao;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.FonteDeEngajamento;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.ParticipanteDoMes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mini-lab do Desafio 2: o caso de uso da premiacao rodando SEM Spring e SEM banco.
 *
 * Todas as portas de saida sao atendidas por adapters falsos. Nenhuma linha do
 * {@link PremiacaoService} precisou mudar para isso - so a "tomada" onde ele esta ligado.
 */
class PremiacaoServiceUnitarioTest {

    private static final String MES = "2026-09";

    private final Map<String, List<ParticipanteDoMes>> participacoesPorMes = new HashMap<>();
    private final Map<Long, Integer> cursosCreditados = new HashMap<>();
    private final List<String> chamadasAFonte = new ArrayList<>();
    private final List<ResultadoPremiacao> eventosPublicados = new ArrayList<>();

    private PremiacaoService premiacao;

    @BeforeEach
    void ligarAdaptersFalsos() {
        FonteDeEngajamento fonteFalsa = mes -> {
            chamadasAFonte.add(mes);
            return participacoesPorMes.getOrDefault(mes, List.of());
        };
        CreditoDeCursos creditoFalso = cursosCreditados::put;
        EventosDePremiacao eventosFalsos = (alunoId, premiacaoConcedida) -> eventosPublicados.add(premiacaoConcedida);
        premiacao = new PremiacaoService(fonteFalsa, creditoFalso, new HistoricoDePremiacoesEmMemoria(), eventosFalsos);
    }

    @Test
    @DisplayName("Premia o aluno de maior engajamento e credita o curso pela porta de saida")
    void premiaOMaiorEngajamento() {
        participacoes(MES, participante(1L, new Aluno("Ana"), 10, 5), participante(2L, new Aluno("Bruno"), 3, 4));

        ResultadoPremiacao resultado = premiacao.encerrarMes(MES);

        assertEquals(new ResultadoPremiacao(MES, "Ana", 15, 1), resultado);
        assertEquals(Map.of(1L, 1), cursosCreditados);
        assertEquals(List.of(resultado), eventosPublicados, "Desafio 4: a premiacao deve publicar exatamente 1 evento");
    }

    @Test
    @DisplayName("Mes sem participacao nao premia nem credita ninguem")
    void mesVazioNaoPremia() {
        ResultadoPremiacao resultado = premiacao.encerrarMes("2026-07");

        assertFalse(resultado.houvePremiacao());
        assertTrue(cursosCreditados.isEmpty());
        assertTrue(eventosPublicados.isEmpty(), "sem premiacao, sem evento");
    }

    @Test
    @DisplayName("Encerrar de novo um mes ja premiado devolve o mesmo resultado e nao credita outro curso")
    void mesJaPremiadoNaoPremiaDeNovo() {
        participacoes(MES, participante(3L, new Aluno("Carla"), 12, 8));

        ResultadoPremiacao primeira = premiacao.encerrarMes(MES);
        ResultadoPremiacao segunda = premiacao.encerrarMes(MES);

        assertEquals(primeira, segunda);
        assertEquals(1, segunda.cursosGanhos());
        assertEquals(List.of(MES), chamadasAFonte, "a fonte de engajamento so deve ser consultada uma vez");
        assertEquals(1, eventosPublicados.size(), "encerrar de novo nao pode notificar de novo");
    }

    @Test
    @DisplayName("Com duas participacoes do mesmo aluno, informa o engajamento da maior")
    void usaAParticipacaoVencedoraDoAluno() {
        Aluno ana = new Aluno("Ana");
        participacoes(MES,
                participante(1L, ana, 1, 1),
                participante(2L, new Aluno("Bruno"), 3, 4),
                participante(1L, ana, 10, 10),
                participante(1L, ana, 2, 2));

        ResultadoPremiacao resultado = premiacao.encerrarMes(MES);

        assertEquals("Ana", resultado.alunoPremiado());
        assertEquals(20, resultado.engajamento());
    }

    @Test
    @DisplayName("Mes fora do formato e rejeitado antes de consultar qualquer porta")
    void mesInvalidoERejeitado() {
        assertThrows(IllegalArgumentException.class, () -> premiacao.encerrarMes("setembro"));
        assertTrue(chamadasAFonte.isEmpty());
    }

    private void participacoes(String mes, ParticipanteDoMes... participantes) {
        participacoesPorMes.put(mes, List.of(participantes));
    }

    private static ParticipanteDoMes participante(Long id, Aluno aluno, int topicos, int comentarios) {
        return new ParticipanteDoMes(id, new ParticipacaoForum(aluno, topicos, comentarios));
    }
}
