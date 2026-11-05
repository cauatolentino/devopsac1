package com.example.grupo12_praticaatdd.engajamento.web;

import com.example.grupo12_praticaatdd.compartilhado.RecursoNaoEncontradoException;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.EngajamentoService;
import com.example.grupo12_praticaatdd.engajamento.aplicacao.ParticipacaoResumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do contrato HTTP do modulo engajamento.
 */
@WebMvcTest(EngajamentoController.class)
class EngajamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EngajamentoService engajamentoService;

    @Test
    @DisplayName("POST /api/participacoes devolve 201 com o engajamento calculado")
    void deveRegistrarParticipacao() throws Exception {
        given(engajamentoService.registrar("Ana", 10, 5, "2026-09"))
                .willReturn(new ParticipacaoResumo(1L, 7L, "Ana", 10, 5, 15, "2026-09"));

        mockMvc.perform(post("/api/participacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomeAluno":"Ana","topicosEscritos":10,"comentariosAjuda":5,
                                 "mesReferencia":"2026-09"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.engajamento").value(15))
                .andExpect(jsonPath("$.alunoId").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/forum/engajamento devolve o ranking do mes")
    void deveDevolverORankingDoMes() throws Exception {
        given(engajamentoService.rankingDoMes("2026-09")).willReturn(List.of(
                new ParticipacaoResumo(1L, 7L, "Ana", 10, 5, 15, "2026-09"),
                new ParticipacaoResumo(2L, 8L, "Bruno", 3, 4, 7, "2026-09")));

        mockMvc.perform(get("/api/forum/engajamento").param("mes", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeAluno").value("Ana"))
                .andExpect(jsonPath("$[1].engajamento").value(7));
    }

    @Test
    @DisplayName("Aluno inexistente vira 404 com o corpo de erro padrao")
    void alunoInexistenteDeve404() throws Exception {
        willThrow(new RecursoNaoEncontradoException("Aluno nao encontrado: Fulano"))
                .given(engajamentoService).registrar("Fulano", 1, 1, "2026-09");

        mockMvc.perform(post("/api/participacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomeAluno":"Fulano","topicosEscritos":1,"comentariosAjuda":1,
                                 "mesReferencia":"2026-09"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Aluno nao encontrado: Fulano"));
    }

    @Test
    @DisplayName("Mes fora do formato AAAA-MM vira 400")
    void mesInvalidoDeve400() throws Exception {
        given(engajamentoService.rankingDoMes("setembro"))
                .willThrow(new IllegalArgumentException(
                        "Mes de referencia deve estar no formato AAAA-MM. Recebido: setembro"));

        mockMvc.perform(get("/api/forum/engajamento").param("mes", "setembro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
