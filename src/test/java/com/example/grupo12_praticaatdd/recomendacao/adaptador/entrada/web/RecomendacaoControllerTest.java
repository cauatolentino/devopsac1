package com.example.grupo12_praticaatdd.recomendacao.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.compartilhado.RecursoNaoEncontradoException;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.RecomendacaoService;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.Recomendacoes;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CatalogoPadrao;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do contrato HTTP do modulo recomendacao.
 */
@WebMvcTest(RecomendacaoController.class)
class RecomendacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecomendacaoService recomendacaoService;

    @Test
    @DisplayName("GET /api/recomendacoes devolve as sugestoes da IA")
    void sugestoesDaIa() throws Exception {
        given(recomendacaoService.recomendar("Carla")).willReturn(new Recomendacoes("Carla",
                List.of(new CursoRecomendado("Escrita tecnica", "perfil autor")),
                Recomendacoes.ORIGEM_IA, false, "heuristico-v1"));

        mockMvc.perform(get("/api/recomendacoes").param("aluno", "Carla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursos[0].titulo").value("Escrita tecnica"))
                .andExpect(jsonPath("$.origem").value("ia-recomendacao"))
                .andExpect(jsonPath("$.degradado").value(false))
                .andExpect(jsonPath("$.versaoModelo").value("heuristico-v1"));
    }

    @Test
    @DisplayName("Com a IA fora, a resposta continua 200 e sinaliza degradado")
    void respostaDegradadaContinua200() throws Exception {
        given(recomendacaoService.recomendar("Ana")).willReturn(new Recomendacoes("Ana",
                CatalogoPadrao.cursosIntrodutorios(), Recomendacoes.ORIGEM_FALLBACK, true, null));

        mockMvc.perform(get("/api/recomendacoes").param("aluno", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degradado").value(true))
                .andExpect(jsonPath("$.origem").value("catalogo-padrao"));
    }

    @Test
    @DisplayName("Aluno inexistente vira 404")
    void alunoInexistente() throws Exception {
        given(recomendacaoService.recomendar("Fulano"))
                .willThrow(new RecursoNaoEncontradoException("Aluno nao encontrado: Fulano"));

        mockMvc.perform(get("/api/recomendacoes").param("aluno", "Fulano"))
                .andExpect(status().isNotFound());
    }
}
