package com.example.grupo12_praticaatdd.aluno.persistencia;

import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integracao da persistencia do modulo aluno, contra o H2 em memoria.
 */
@DataJpaTest
class AlunoRepositoryTest {

    @Autowired
    private AlunoRepository alunoRepository;

    @Test
    @DisplayName("Deve salvar e recuperar um aluno pelo nome")
    void deveSalvarERecuperarAlunoPeloNome() {
        alunoRepository.save(new AlunoEntity("Ana"));

        Optional<AlunoEntity> encontrado = alunoRepository.findByNome("Ana");

        assertTrue(encontrado.isPresent());
        assertEquals("Ana", encontrado.get().getNome());
        assertEquals(0, encontrado.get().getCursosGanhos());
        assertTrue(alunoRepository.existsByNome("Ana"));
        assertFalse(alunoRepository.existsByNome("Bruno"));
    }

    @Test
    @DisplayName("A entidade converte para o objeto de dominio preservando os cursos ganhos")
    void entidadeDeveConverterParaDominio() {
        AlunoEntity entidade = new AlunoEntity("Ana");
        entidade.setCursosGanhos(3);

        Aluno dominio = alunoRepository.save(entidade).paraDominio();

        assertEquals("Ana", dominio.getNome());
        assertEquals(3, dominio.getCursosGanhos());
    }
}
