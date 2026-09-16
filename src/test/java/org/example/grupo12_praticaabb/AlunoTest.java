package org.example.grupo12_praticaabb;

import org.example.grupo12_praticaabb.Domain.Aluno;
import org.example.grupo12_praticaabb.Domain.Forum;
import org.example.grupo12_praticaabb.Domain.ParticipacaoForum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AlunoTest {

    @Test
    public void PremiarAlunoTest() {

        var forum = new Forum();

        var ana = new Aluno("Ana");
        var bruno = new Aluno("Bruno");

        forum.registrar(new ParticipacaoForum(ana, 10, 5));
        forum.registrar(new ParticipacaoForum(bruno, 3, 4));

        var premiado = forum.premiarAluno();

        assertEquals("Ana", premiado.getNome());
    }

    @Test
    public void DevePremiarSegundoAlunoQuandoEleTiverMaiorEngajamento() {

        var forum = new Forum();

        var ana = new Aluno("Ana");
        var bruno = new Aluno("Bruno");

        forum.registrar(new ParticipacaoForum(ana, 3, 4));
        forum.registrar(new ParticipacaoForum(bruno, 10, 5));

        var premiado = forum.premiarAluno();

        assertEquals("Bruno", premiado.getNome());
    }

    @Test
    public void DeveRetornarNullQuandoForumEstiverVazio() {

        var forum = new Forum();

        var premiado = forum.premiarAluno();

        assertNull(premiado);
    }
}
