package com.example.grupo12_praticaatdd.premiacao.aplicacao;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoResumo;
import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.EngajamentoService;
import com.example.grupo12_praticaatdd.eventos.aplicacao.RegistroDeEventos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integracao do modulo premiacao contra o H2 em memoria.
 *
 * Os mesmos cenarios BDD da planilha, agora atravessando tres modulos:
 * aluno (cadastro), engajamento (participacoes) e premiacao (regra da US1).
 */
@SpringBootTest
@Transactional
class PremiacaoServiceTest {

    private static final String MES = "2026-09";

    @Autowired
    private PremiacaoService premiacaoService;

    @Autowired
    private EngajamentoService engajamentoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private RegistroDeEventos registroDeEventos;

    @Test
    @DisplayName("Cenario 1 - encerrar o mes premia o aluno de maior engajamento com 1 curso")
    void devePremiarAlunoComMaiorEngajamento() {
        alunoService.cadastrar("Ana");
        alunoService.cadastrar("Bruno");
        engajamentoService.registrar("Ana", 10, 5, MES);
        engajamentoService.registrar("Bruno", 3, 4, MES);

        ResultadoPremiacao premiacao = premiacaoService.encerrarMes(MES);

        assertTrue(premiacao.houvePremiacao());
        assertEquals("Ana", premiacao.alunoPremiado());
        assertEquals(15, premiacao.engajamento());
        assertEquals(1, premiacao.cursosGanhos());
    }

    @Test
    @DisplayName("Cenario 3 - somente o aluno de maior engajamento recebe o curso")
    void somenteOMaiorEngajamentoRecebeOCurso() {
        alunoService.cadastrar("Ana");
        alunoService.cadastrar("Bruno");
        alunoService.cadastrar("Carla");
        engajamentoService.registrar("Bruno", 3, 4, MES);
        engajamentoService.registrar("Ana", 2, 1, MES);
        engajamentoService.registrar("Carla", 12, 8, MES);

        premiacaoService.encerrarMes(MES);

        List<AlunoResumo> alunos = alunoService.listar();
        assertEquals(1, cursosDe(alunos, "Carla"));
        assertEquals(0, cursosDe(alunos, "Ana"));
        assertEquals(0, cursosDe(alunos, "Bruno"));
    }

    @Test
    @DisplayName("Cenario 4 - mes sem participacao nao premia ninguem")
    void mesSemParticipacaoNaoPremiaNinguem() {
        ResultadoPremiacao premiacao = premiacaoService.encerrarMes("2026-07");

        assertFalse(premiacao.houvePremiacao());
        assertNull(premiacao.alunoPremiado());
        assertEquals("2026-07", premiacao.mesReferencia());
    }

    @Test
    @DisplayName("Aluno ja premiado acumula cursos e o engajamento informado e o da participacao vencedora")
    void premiacaoAcumulaCursosEUsaParticipacaoVencedora() {
        alunoService.cadastrar("Ana");
        alunoService.cadastrar("Bruno");
        engajamentoService.registrar("Ana", 1, 1, MES);
        engajamentoService.registrar("Ana", 10, 10, MES);
        engajamentoService.registrar("Bruno", 3, 4, MES);
        ResultadoPremiacao setembro = premiacaoService.encerrarMes(MES);

        engajamentoService.registrar("Ana", 5, 5, "2026-10");
        ResultadoPremiacao outubro = premiacaoService.encerrarMes("2026-10");

        assertEquals("Ana", setembro.alunoPremiado());
        assertEquals(20, setembro.engajamento());
        assertEquals("Ana", outubro.alunoPremiado());
        assertEquals(10, outubro.engajamento());
        assertEquals(2, outubro.cursosGanhos());
        assertEquals(2, cursosDe(alunoService.listar(), "Ana"));
    }

    @Test
    @DisplayName("Desafio 2 - encerrar o mesmo mes tres vezes credita um unico curso (adapter JPA)")
    void mesmoMesNaoEPremiadoDuasVezes() {
        alunoService.cadastrar("Carla");
        engajamentoService.registrar("Carla", 12, 8, MES);

        long eventosAntes = registroDeEventos.pendentes();
        ResultadoPremiacao primeira = premiacaoService.encerrarMes(MES);
        premiacaoService.encerrarMes(MES);
        ResultadoPremiacao terceira = premiacaoService.encerrarMes(MES);

        assertEquals(primeira, terceira);
        assertEquals(1, cursosDe(alunoService.listar(), "Carla"));
        assertEquals(eventosAntes + 1, registroDeEventos.pendentes(),
                "Desafio 4: 1 evento no outbox, na mesma transacao, e nenhum nas repeticoes");
    }

    @Test
    @DisplayName("Mes de referencia fora do formato AAAA-MM e rejeitado")
    void naoDeveAceitarMesForaDoFormato() {
        assertThrows(IllegalArgumentException.class, () -> premiacaoService.encerrarMes("setembro"));
    }

    private int cursosDe(List<AlunoResumo> alunos, String nome) {
        return alunos.stream()
                .filter(aluno -> aluno.nome().equals(nome))
                .findFirst()
                .orElseThrow()
                .cursosGanhos();
    }
}
