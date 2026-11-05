package com.example.grupo12_praticaatdd.engajamento.aplicacao;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.compartilhado.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integracao do contrato publico do modulo engajamento, contra o H2 em memoria.
 */
@SpringBootTest
@Transactional
class EngajamentoServiceTest {

    private static final String MES = "2026-09";

    @Autowired
    private EngajamentoService engajamentoService;

    @Autowired
    private AlunoService alunoService;

    @Test
    @DisplayName("Registrar participacao devolve o engajamento calculado pelo dominio")
    void deveRegistrarParticipacaoComEngajamento() {
        Long anaId = alunoService.cadastrar("Ana").id();

        ParticipacaoResumo participacao = engajamentoService.registrar("Ana", 10, 5, MES);

        assertEquals(anaId, participacao.alunoId());
        assertEquals("Ana", participacao.nomeAluno());
        assertEquals(15, participacao.engajamento());
        assertEquals(MES, participacao.mesReferencia());
    }

    @Test
    @DisplayName("Ranking do mes vem ordenado do maior para o menor engajamento")
    void rankingDeveVirOrdenadoPorEngajamento() {
        alunoService.cadastrar("Ana");
        alunoService.cadastrar("Bruno");
        engajamentoService.registrar("Bruno", 3, 4, MES);
        engajamentoService.registrar("Ana", 10, 5, MES);

        List<ParticipacaoResumo> ranking = engajamentoService.rankingDoMes(MES);

        assertEquals(2, ranking.size());
        assertEquals("Ana", ranking.get(0).nomeAluno());
        assertEquals(15, ranking.get(0).engajamento());
        assertEquals("Bruno", ranking.get(1).nomeAluno());
        assertEquals(7, ranking.get(1).engajamento());
    }

    @Test
    @DisplayName("Totais do aluno somam as participacoes de todos os meses")
    void totaisDoAlunoSomamTodosOsMeses() {
        Long anaId = alunoService.cadastrar("Ana").id();
        alunoService.cadastrar("Bruno");
        engajamentoService.registrar("Ana", 10, 5, "2026-08");
        engajamentoService.registrar("Ana", 2, 3, MES);
        engajamentoService.registrar("Bruno", 7, 7, MES);

        TotaisDeEngajamento totais = engajamentoService.totaisDoAluno(anaId);

        assertEquals(new TotaisDeEngajamento(12, 8), totais);
    }

    @Test
    @DisplayName("Participacao de aluno inexistente resulta em recurso nao encontrado")
    void naoDeveRegistrarParticipacaoDeAlunoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> engajamentoService.registrar("Fulano", 1, 1, MES));
    }

    @Test
    @DisplayName("Quantidade negativa e barrada pela regra do dominio")
    void naoDeveRegistrarParticipacaoNegativa() {
        alunoService.cadastrar("Ana");

        assertThrows(IllegalArgumentException.class,
                () -> engajamentoService.registrar("Ana", -1, 0, MES));
    }

    @Test
    @DisplayName("Mes de referencia fora do formato AAAA-MM e rejeitado")
    void naoDeveAceitarMesForaDoFormato() {
        assertThrows(IllegalArgumentException.class, () -> engajamentoService.rankingDoMes(null));
        assertThrows(IllegalArgumentException.class,
                () -> engajamentoService.registrar("Ana", 1, 1, "setembro"));
    }
}
