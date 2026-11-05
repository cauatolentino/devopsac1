package com.example.grupo12_praticaatdd.aluno.web;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoResumo;
import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
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
 * Testes do contrato HTTP do modulo aluno.
 */
@WebMvcTest(AlunoController.class)
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlunoService alunoService;

    @Test
    @DisplayName("POST /api/alunos devolve 201 com o aluno criado")
    void deveCadastrarAluno() throws Exception {
        given(alunoService.cadastrar("Ana")).willReturn(new AlunoResumo(1L, "Ana", 0));

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.cursosGanhos").value(0));
    }

    @Test
    @DisplayName("GET /api/alunos devolve a lista de alunos")
    void deveListarAlunos() throws Exception {
        given(alunoService.listar())
                .willReturn(List.of(new AlunoResumo(1L, "Ana", 1), new AlunoResumo(2L, "Bruno", 0)));

        mockMvc.perform(get("/api/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[0].cursosGanhos").value(1));
    }

    @Test
    @DisplayName("Violacao de regra do dominio vira 400 com o corpo de erro padrao")
    void regraDeNegocioVioladaDeve400() throws Exception {
        willThrow(new IllegalArgumentException("Nome do aluno e obrigatorio"))
                .given(alunoService).cadastrar("  ");

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Nome do aluno e obrigatorio"));
    }
}
