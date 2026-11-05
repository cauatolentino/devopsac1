package com.example.grupo12_praticaatdd.premiacao.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.entrada.EncerrarMes;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do adapter de entrada HTTP do modulo premiacao (a porta EncerrarMes e simulada).
 */
@WebMvcTest(PremiacaoController.class)
class PremiacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EncerrarMes encerrarMes;

    @Test
    @DisplayName("POST /api/forum/encerrar-mes devolve o aluno premiado")
    void deveEncerrarOMesEPremiar() throws Exception {
        given(encerrarMes.encerrarMes("2026-09"))
                .willReturn(new ResultadoPremiacao("2026-09", "Ana", 15, 1));

        mockMvc.perform(post("/api/forum/encerrar-mes").param("mes", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houvePremiacao").value(true))
                .andExpect(jsonPath("$.alunoPremiado").value("Ana"))
                .andExpect(jsonPath("$.engajamento").value(15))
                .andExpect(jsonPath("$.cursosGanhos").value(1))
                .andExpect(jsonPath("$.mensagem").value("Ana foi o aluno de maior engajamento em 2026-09 e ganhou 1 curso."));
    }

    @Test
    @DisplayName("Mes sem participacao devolve 200 sem premiacao")
    void deveEncerrarMesSemPremiacao() throws Exception {
        given(encerrarMes.encerrarMes("2026-07"))
                .willReturn(ResultadoPremiacao.semPremiacao("2026-07"));

        mockMvc.perform(post("/api/forum/encerrar-mes").param("mes", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houvePremiacao").value(false))
                .andExpect(jsonPath("$.alunoPremiado").doesNotExist());
    }
}
