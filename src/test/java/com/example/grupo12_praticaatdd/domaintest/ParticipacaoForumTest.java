package com.example.grupo12_praticaatdd.domaintest;

import com.example.grupo12_praticaatdd.domain.Aluno;
import com.example.grupo12_praticaatdd.domain.ParticipacaoForum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de unidade da ParticipacaoForum.
 *
 * CENARIO 2 do BDD (autor: Pedro Pizzi)
 *   DADO alunos com diferentes quantidades de topicos e comentarios de ajuda
 *   QUANDO o engajamento de um aluno e calculado
 *   ENTAO deve ser a soma dos topicos com os comentarios de ajuda
 */
class ParticipacaoForumTest {

    @Test
    @DisplayName("Cenario 2 - engajamento e a soma de topicos e comentarios de ajuda")
    void engajamentoDeveSomarTopicosEComentarios() {
        // DADO
        Aluno ana = new Aluno("Ana");
        ParticipacaoForum participacao = new ParticipacaoForum(ana, 10, 5);

        // QUANDO
        int engajamento = participacao.getEngajamento();

        // ENTAO
        assertEquals(15, engajamento);
    }

    @Test
    @DisplayName("Participacao guarda o aluno e os numeros informados")
    void deveExporOsDadosDaParticipacao() {
        Aluno bruno = new Aluno("Bruno");

        ParticipacaoForum participacao = new ParticipacaoForum(bruno, 3, 4);

        assertEquals(bruno, participacao.getAluno());
        assertEquals(3, participacao.getTopicosEscritos());
        assertEquals(4, participacao.getComentariosAjuda());
    }

    @Test
    @DisplayName("Nao deve registrar participacao sem aluno")
    void naoDeveCriarParticipacaoSemAluno() {
        assertThrows(IllegalArgumentException.class, () -> new ParticipacaoForum(null, 1, 1));
    }

    @Test
    @DisplayName("Nao deve aceitar quantidade negativa de topicos")
    void naoDeveCriarParticipacaoComTopicosNegativos() {
        Aluno ana = new Aluno("Ana");

        assertThrows(IllegalArgumentException.class, () -> new ParticipacaoForum(ana, -1, 0));
    }

    @Test
    @DisplayName("Nao deve aceitar quantidade negativa de comentarios de ajuda")
    void naoDeveCriarParticipacaoComComentariosNegativos() {
        Aluno ana = new Aluno("Ana");

        assertThrows(IllegalArgumentException.class, () -> new ParticipacaoForum(ana, 0, -1));
    }
}
