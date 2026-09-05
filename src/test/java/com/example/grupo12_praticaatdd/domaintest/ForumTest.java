package com.example.grupo12_praticaatdd.domaintest;

import com.example.grupo12_praticaatdd.domain.Aluno;
import com.example.grupo12_praticaatdd.domain.Forum;
import com.example.grupo12_praticaatdd.domain.ParticipacaoForum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de unidade do Forum - regra principal da US1.
 *
 * CENARIO 1 (autor: Caua Tolentino)
 *   DADO um forum com a participacao de varios alunos no mes
 *   E com engajamentos diferentes
 *   QUANDO o mes e encerrado
 *   ENTAO o aluno com maior engajamento deve ganhar 1 curso
 *
 * CENARIO 3 (autor: Henry Tanaka)
 *   DADO um forum com varios alunos participando
 *   QUANDO o mes e encerrado
 *   ENTAO somente o aluno de maior engajamento e premiado
 *
 * CENARIO 4 (autor: Caua Tolentino)
 *   DADO um forum sem nenhuma participacao no mes
 *   QUANDO o mes e encerrado
 *   ENTAO nenhum aluno deve ser premiado
 */
class ForumTest {

    @Test
    @DisplayName("Cenario 1 - o aluno com maior engajamento ganha 1 curso")
    void devePremiarAlunoComMaiorEngajamento() {
        // DADO um forum com varios alunos e engajamentos diferentes
        Forum forum = new Forum();
        Aluno ana = new Aluno("Ana");
        Aluno bruno = new Aluno("Bruno");
        forum.registrar(new ParticipacaoForum(ana, 10, 5));
        forum.registrar(new ParticipacaoForum(bruno, 3, 4));

        // QUANDO o mes e encerrado
        Aluno premiado = forum.premiarAlunoDoMes();

        // ENTAO
        assertEquals("Ana", premiado.getNome());
        assertEquals(1, ana.getCursosGanhos());
    }

    @Test
    @DisplayName("Cenario 3 - somente o aluno de maior engajamento e premiado")
    void somenteAlunoDeMaiorEngajamentoEPremiado() {
        // DADO um forum com varios alunos participando
        Forum forum = new Forum();
        Aluno ana = new Aluno("Ana");
        Aluno bruno = new Aluno("Bruno");
        Aluno carla = new Aluno("Carla");
        forum.registrar(new ParticipacaoForum(bruno, 3, 4));
        forum.registrar(new ParticipacaoForum(ana, 2, 1));
        forum.registrar(new ParticipacaoForum(carla, 12, 8));

        // QUANDO o mes e encerrado
        Aluno premiado = forum.premiarAlunoDoMes();

        // ENTAO somente Carla e premiada
        assertEquals("Carla", premiado.getNome());
        assertEquals(1, carla.getCursosGanhos());
        assertEquals(0, ana.getCursosGanhos());
        assertEquals(0, bruno.getCursosGanhos());
    }

    @Test
    @DisplayName("Cenario 4 - forum sem participacao nao premia ninguem")
    void naoDevePremiarNinguemQuandoNaoHouveParticipacao() {
        // DADO um forum sem nenhuma participacao no mes
        Forum forum = new Forum();

        // QUANDO o mes e encerrado
        Aluno premiado = forum.premiarAlunoDoMes();

        // ENTAO
        assertNull(premiado);
    }

    @Test
    @DisplayName("Nao deve registrar participacao nula")
    void naoDeveRegistrarParticipacaoNula() {
        Forum forum = new Forum();

        assertThrows(IllegalArgumentException.class, () -> forum.registrar(null));
    }

    @Test
    @DisplayName("As participacoes registradas ficam disponiveis para consulta")
    void deveListarAsParticipacoesRegistradas() {
        Forum forum = new Forum();
        Aluno ana = new Aluno("Ana");
        ParticipacaoForum participacao = new ParticipacaoForum(ana, 10, 5);

        forum.registrar(participacao);

        assertEquals(1, forum.getParticipacoes().size());
        assertEquals(participacao, forum.getParticipacoes().get(0));
    }

    @Test
    @DisplayName("A lista de participacoes exposta e imutavel")
    void naoDevePermitirAlterarAListaExposta() {
        Forum forum = new Forum();
        ParticipacaoForum participacao = new ParticipacaoForum(new Aluno("Ana"), 1, 1);

        assertThrows(UnsupportedOperationException.class, () -> forum.getParticipacoes().add(participacao));
    }
}
