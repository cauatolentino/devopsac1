package com.example.grupo12_praticaatdd.repository;

import com.example.grupo12_praticaatdd.domain.Aluno;
import com.example.grupo12_praticaatdd.entity.AlunoEntity;
import com.example.grupo12_praticaatdd.entity.ParticipacaoForumEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integracao da camada de persistencia, rodando contra o H2 em memoria.
 */
@DataJpaTest
class ParticipacaoForumRepositoryTest {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ParticipacaoForumRepository participacaoRepository;

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
    @DisplayName("Deve buscar somente as participacoes do mes de referencia informado")
    void deveFiltrarParticipacoesPorMesDeReferencia() {
        AlunoEntity ana = alunoRepository.save(new AlunoEntity("Ana"));
        AlunoEntity bruno = alunoRepository.save(new AlunoEntity("Bruno"));
        participacaoRepository.save(new ParticipacaoForumEntity(ana, 10, 5, "2026-09"));
        participacaoRepository.save(new ParticipacaoForumEntity(bruno, 3, 4, "2026-09"));
        participacaoRepository.save(new ParticipacaoForumEntity(ana, 1, 1, "2026-08"));

        List<ParticipacaoForumEntity> setembro = participacaoRepository.findByMesReferencia("2026-09");

        assertEquals(2, setembro.size());
        assertTrue(setembro.stream().allMatch(p -> "2026-09".equals(p.getMesReferencia())));
    }

    @Test
    @DisplayName("Deve persistir os numeros da participacao e o vinculo com o aluno")
    void devePersistirOsDadosDaParticipacao() {
        AlunoEntity ana = alunoRepository.save(new AlunoEntity("Ana"));

        ParticipacaoForumEntity salva =
                participacaoRepository.save(new ParticipacaoForumEntity(ana, 10, 5, "2026-09"));

        ParticipacaoForumEntity recuperada = participacaoRepository.findById(salva.getId()).orElseThrow();
        assertEquals(10, recuperada.getTopicosEscritos());
        assertEquals(5, recuperada.getComentariosAjuda());
        assertEquals("2026-09", recuperada.getMesReferencia());
        assertEquals("Ana", recuperada.getAluno().getNome());
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
