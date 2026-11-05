package com.example.grupo12_praticaatdd.engajamento.persistencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integracao da persistencia do modulo engajamento, contra o H2 em memoria.
 *
 * O modulo nao depende da persistencia do modulo aluno: a participacao guarda so o id do aluno.
 */
@DataJpaTest
class ParticipacaoForumRepositoryTest {

    private static final Long ANA = 1L;
    private static final Long BRUNO = 2L;

    @Autowired
    private ParticipacaoForumRepository participacaoRepository;

    @Test
    @DisplayName("Deve buscar somente as participacoes do mes de referencia informado")
    void deveFiltrarParticipacoesPorMesDeReferencia() {
        participacaoRepository.save(new ParticipacaoForumEntity(ANA, 10, 5, "2026-09"));
        participacaoRepository.save(new ParticipacaoForumEntity(BRUNO, 3, 4, "2026-09"));
        participacaoRepository.save(new ParticipacaoForumEntity(ANA, 1, 1, "2026-08"));

        List<ParticipacaoForumEntity> setembro = participacaoRepository.findByMesReferencia("2026-09");

        assertEquals(2, setembro.size());
        assertTrue(setembro.stream().allMatch(p -> "2026-09".equals(p.getMesReferencia())));
    }

    @Test
    @DisplayName("Deve persistir os numeros da participacao e o vinculo com o aluno")
    void devePersistirOsDadosDaParticipacao() {
        ParticipacaoForumEntity salva =
                participacaoRepository.save(new ParticipacaoForumEntity(ANA, 10, 5, "2026-09"));

        ParticipacaoForumEntity recuperada = participacaoRepository.findById(salva.getId()).orElseThrow();
        assertEquals(10, recuperada.getTopicosEscritos());
        assertEquals(5, recuperada.getComentariosAjuda());
        assertEquals("2026-09", recuperada.getMesReferencia());
        assertEquals(ANA, recuperada.getAlunoId());
    }
}
