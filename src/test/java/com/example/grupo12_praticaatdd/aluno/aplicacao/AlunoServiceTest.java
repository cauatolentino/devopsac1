package com.example.grupo12_praticaatdd.aluno.aplicacao;

import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import com.example.grupo12_praticaatdd.compartilhado.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integracao do contrato publico do modulo aluno, contra o H2 em memoria.
 */
@SpringBootTest
@Transactional
class AlunoServiceTest {

    @Autowired
    private AlunoService alunoService;

    @Test
    @DisplayName("Cadastra e lista alunos com zero cursos ganhos")
    void deveCadastrarEListarAlunos() {
        alunoService.cadastrar("Ana");
        alunoService.cadastrar("  Bruno ");

        List<AlunoResumo> alunos = alunoService.listar();

        assertEquals(List.of("Ana", "Bruno"), alunos.stream().map(AlunoResumo::nome).toList());
        assertTrue(alunos.stream().allMatch(aluno -> aluno.cursosGanhos() == 0));
    }

    @Test
    @DisplayName("Nao deve cadastrar dois alunos com o mesmo nome")
    void naoDeveCadastrarAlunoDuplicado() {
        alunoService.cadastrar("Ana");

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> alunoService.cadastrar("Ana"));

        assertTrue(erro.getMessage().contains("Ja existe um aluno"));
    }

    @Test
    @DisplayName("Nome em branco e barrado pela regra do dominio")
    void naoDeveCadastrarAlunoSemNome() {
        assertThrows(IllegalArgumentException.class, () -> alunoService.cadastrar("  "));
    }

    @Test
    @DisplayName("Buscar aluno inexistente resulta em recurso nao encontrado")
    void buscarAlunoInexistenteDeveFalhar() {
        assertThrows(RecursoNaoEncontradoException.class, () -> alunoService.buscarPorNome("Fulano"));
    }

    @Test
    @DisplayName("Carrega alunos como objetos de dominio e persiste os cursos calculados pelo dominio")
    void deveCarregarAlunosEAtualizarCursos() {
        AlunoResumo ana = alunoService.cadastrar("Ana");

        AlunoResumo atualizado = alunoService.atualizarCursosGanhos(ana.id(), 2);
        Map<Long, Aluno> alunos = alunoService.carregarAlunos(List.of(ana.id()));

        assertEquals(2, atualizado.cursosGanhos());
        assertEquals("Ana", alunos.get(ana.id()).getNome());
        assertEquals(2, alunos.get(ana.id()).getCursosGanhos());
        assertEquals(ana.id(), alunoService.buscarPorNome("Ana").id());
    }

    @Test
    @DisplayName("Atualizar cursos de um aluno inexistente resulta em recurso nao encontrado")
    void atualizarAlunoInexistenteDeveFalhar() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> alunoService.atualizarCursosGanhos(999L, 1));
    }
}
