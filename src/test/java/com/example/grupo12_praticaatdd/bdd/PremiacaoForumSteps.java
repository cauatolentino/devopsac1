package com.example.grupo12_praticaatdd.bdd;

import com.example.grupo12_praticaatdd.domain.Aluno;
import com.example.grupo12_praticaatdd.domain.Forum;
import com.example.grupo12_praticaatdd.domain.ParticipacaoForum;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Implementacao dos passos dos cenarios BDD.
 *
 * Os passos falam diretamente com o pacote `domain`, o mesmo codigo construido
 * pelo ciclo TDD. E isso que fecha o ATDD: o criterio de aceite escrito na
 * planilha executa contra a regra de negocio de verdade.
 */
public class PremiacaoForumSteps {

    private Forum forum = new Forum();
    private final Map<String, Aluno> alunos = new LinkedHashMap<>();
    private ParticipacaoForum participacaoEmAnalise;
    private Aluno premiado;
    private int engajamentoCalculado;

    @Dado("um fórum sem nenhuma participação no mês")
    public void umForumSemParticipacoes() {
        forum = new Forum();
        alunos.clear();
        premiado = null;
    }

    @Dado("um fórum com a participação de vários alunos no mês")
    public void umForumComParticipacoes(List<Map<String, String>> participacoes) {
        forum = new Forum();
        alunos.clear();

        for (Map<String, String> linha : participacoes) {
            Aluno aluno = new Aluno(linha.get("aluno"));
            alunos.put(aluno.getNome(), aluno);
            forum.registrar(new ParticipacaoForum(aluno,
                    Integer.parseInt(linha.get("topicos")),
                    Integer.parseInt(linha.get("comentarios"))));
        }
    }

    @E("com engajamentos diferentes")
    public void comEngajamentosDiferentes() {
        long engajamentosDistintos = forum.getParticipacoes().stream()
                .map(ParticipacaoForum::getEngajamento)
                .distinct()
                .count();

        assertEquals(forum.getParticipacoes().size(), engajamentosDistintos,
                "os alunos do cenario precisam ter engajamentos diferentes");
    }

    @Dado("que o aluno {string} escreveu {int} tópicos e fez {int} comentários de ajuda")
    public void queOAlunoEscreveu(String nome, int topicos, int comentarios) {
        participacaoEmAnalise = new ParticipacaoForum(new Aluno(nome), topicos, comentarios);
    }

    @Quando("o mês é encerrado")
    public void oMesEEncerrado() {
        premiado = forum.premiarAlunoDoMes();
    }

    @Quando("o engajamento dele é calculado")
    public void oEngajamentoECalculado() {
        engajamentoCalculado = participacaoEmAnalise.getEngajamento();
    }

    @Entao("o aluno {string} deve ser premiado")
    public void oAlunoDeveSerPremiado(String nome) {
        assertNotNull(premiado, "esperava um aluno premiado, mas ninguem foi premiado");
        assertEquals(nome, premiado.getNome());
    }

    @Entao("nenhum aluno deve ser premiado")
    public void nenhumAlunoDeveSerPremiado() {
        assertNull(premiado);
        assertTrue(alunos.values().stream().allMatch(aluno -> aluno.getCursosGanhos() == 0));
    }

    @Entao("o engajamento deve ser {int}")
    public void oEngajamentoDeveSer(int esperado) {
        assertEquals(esperado, engajamentoCalculado);
    }

    @E("{string} deve ter {int} curso ganho")
    public void deveTerCursoGanho(String nome, int cursos) {
        assertEquals(cursos, alunos.get(nome).getCursosGanhos());
    }

    @E("{string} deve ter {int} cursos ganhos")
    public void deveTerCursosGanhos(String nome, int cursos) {
        assertEquals(cursos, alunos.get(nome).getCursosGanhos());
    }
}
