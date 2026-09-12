package com.example.grupo12_praticaatdd.service;

import com.example.grupo12_praticaatdd.dto.AlunoDTO;
import com.example.grupo12_praticaatdd.dto.NovaParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.NovoAlunoDTO;
import com.example.grupo12_praticaatdd.dto.ParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.PremiacaoDTO;
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
 * Testes de integracao do ForumService contra o H2 em memoria.
 *
 * Aqui os mesmos cenarios BDD da planilha sao verificados ponta a ponta na
 * camada de servico: os dados vem do banco, a regra vem do pacote domain.
 */
@SpringBootTest
@Transactional
class ForumServiceTest {

    private static final String MES = "2026-09";

    @Autowired
    private ForumService forumService;

    @Test
    @DisplayName("Cenario 1 - encerrar o mes premia o aluno de maior engajamento com 1 curso")
    void devePremiarAlunoComMaiorEngajamento() {
        forumService.cadastrarAluno(new NovoAlunoDTO("Ana"));
        forumService.cadastrarAluno(new NovoAlunoDTO("Bruno"));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Ana", 10, 5, MES));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Bruno", 3, 4, MES));

        PremiacaoDTO premiacao = forumService.encerrarMes(MES);

        assertTrue(premiacao.houvePremiacao());
        assertEquals("Ana", premiacao.alunoPremiado());
        assertEquals(15, premiacao.engajamento());
        assertEquals(1, premiacao.cursosGanhos());
    }

    @Test
    @DisplayName("Cenario 3 - somente o aluno de maior engajamento recebe o curso")
    void somenteOMaiorEngajamentoRecebeOCurso() {
        forumService.cadastrarAluno(new NovoAlunoDTO("Ana"));
        forumService.cadastrarAluno(new NovoAlunoDTO("Bruno"));
        forumService.cadastrarAluno(new NovoAlunoDTO("Carla"));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Bruno", 3, 4, MES));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Ana", 2, 1, MES));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Carla", 12, 8, MES));

        forumService.encerrarMes(MES);

        List<AlunoDTO> alunos = forumService.listarAlunos();
        assertEquals(1, cursosDe(alunos, "Carla"));
        assertEquals(0, cursosDe(alunos, "Ana"));
        assertEquals(0, cursosDe(alunos, "Bruno"));
    }

    @Test
    @DisplayName("Cenario 4 - mes sem participacao nao premia ninguem")
    void mesSemParticipacaoNaoPremiaNinguem() {
        PremiacaoDTO premiacao = forumService.encerrarMes("2026-07");

        assertFalse(premiacao.houvePremiacao());
        assertNull(premiacao.alunoPremiado());
        assertTrue(premiacao.mensagem().contains("nenhum aluno foi premiado"));
    }

    @Test
    @DisplayName("Ranking do mes vem ordenado do maior para o menor engajamento")
    void rankingDeveVirOrdenadoPorEngajamento() {
        forumService.cadastrarAluno(new NovoAlunoDTO("Ana"));
        forumService.cadastrarAluno(new NovoAlunoDTO("Bruno"));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Bruno", 3, 4, MES));
        forumService.registrarParticipacao(new NovaParticipacaoDTO("Ana", 10, 5, MES));

        List<ParticipacaoDTO> ranking = forumService.listarEngajamentoDoMes(MES);

        assertEquals(2, ranking.size());
        assertEquals("Ana", ranking.get(0).nomeAluno());
        assertEquals(15, ranking.get(0).engajamento());
        assertEquals("Bruno", ranking.get(1).nomeAluno());
        assertEquals(7, ranking.get(1).engajamento());
    }

    @Test
    @DisplayName("Nao deve cadastrar dois alunos com o mesmo nome")
    void naoDeveCadastrarAlunoDuplicado() {
        forumService.cadastrarAluno(new NovoAlunoDTO("Ana"));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> forumService.cadastrarAluno(new NovoAlunoDTO("Ana")));

        assertTrue(erro.getMessage().contains("Ja existe um aluno"));
    }

    @Test
    @DisplayName("Nome em branco e barrado pela regra do dominio")
    void naoDeveCadastrarAlunoSemNome() {
        assertThrows(IllegalArgumentException.class,
                () -> forumService.cadastrarAluno(new NovoAlunoDTO("  ")));
    }

    @Test
    @DisplayName("Participacao de aluno inexistente resulta em recurso nao encontrado")
    void naoDeveRegistrarParticipacaoDeAlunoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> forumService.registrarParticipacao(new NovaParticipacaoDTO("Fulano", 1, 1, MES)));
    }

    @Test
    @DisplayName("Quantidade negativa e barrada pela regra do dominio")
    void naoDeveRegistrarParticipacaoNegativa() {
        forumService.cadastrarAluno(new NovoAlunoDTO("Ana"));

        assertThrows(IllegalArgumentException.class,
                () -> forumService.registrarParticipacao(new NovaParticipacaoDTO("Ana", -1, 0, MES)));
    }

    @Test
    @DisplayName("Mes de referencia fora do formato AAAA-MM e rejeitado")
    void naoDeveAceitarMesForaDoFormato() {
        assertThrows(IllegalArgumentException.class, () -> forumService.encerrarMes("setembro"));
        assertThrows(IllegalArgumentException.class, () -> forumService.listarEngajamentoDoMes(null));
    }

    private int cursosDe(List<AlunoDTO> alunos, String nome) {
        return alunos.stream()
                .filter(aluno -> aluno.nome().equals(nome))
                .findFirst()
                .orElseThrow()
                .cursosGanhos();
    }
}
