package com.example.grupo12_praticaatdd.controller;

import com.example.grupo12_praticaatdd.dto.AlunoDTO;
import com.example.grupo12_praticaatdd.dto.NovaParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.NovoAlunoDTO;
import com.example.grupo12_praticaatdd.dto.ParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.PremiacaoDTO;
import com.example.grupo12_praticaatdd.service.ForumService;
import com.example.grupo12_praticaatdd.service.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do contrato HTTP exposto pelo ForumController.
 */
@WebMvcTest(ForumController.class)
class ForumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForumService forumService;

    @Test
    @DisplayName("POST /api/alunos devolve 201 com o aluno criado")
    void deveCadastrarAluno() throws Exception {
        given(forumService.cadastrarAluno(any(NovoAlunoDTO.class)))
                .willReturn(new AlunoDTO(1L, "Ana", 0));

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
        given(forumService.listarAlunos())
                .willReturn(List.of(new AlunoDTO(1L, "Ana", 1), new AlunoDTO(2L, "Bruno", 0)));

        mockMvc.perform(get("/api/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[0].cursosGanhos").value(1));
    }

    @Test
    @DisplayName("POST /api/participacoes devolve 201 com o engajamento calculado")
    void deveRegistrarParticipacao() throws Exception {
        given(forumService.registrarParticipacao(any(NovaParticipacaoDTO.class)))
                .willReturn(new ParticipacaoDTO(1L, "Ana", 10, 5, 15, "2026-09"));

        mockMvc.perform(post("/api/participacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomeAluno":"Ana","topicosEscritos":10,"comentariosAjuda":5,
                                 "mesReferencia":"2026-09"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.engajamento").value(15));
    }

    @Test
    @DisplayName("GET /api/forum/engajamento devolve o ranking do mes")
    void deveDevolverORankingDoMes() throws Exception {
        given(forumService.listarEngajamentoDoMes("2026-09")).willReturn(List.of(
                new ParticipacaoDTO(1L, "Ana", 10, 5, 15, "2026-09"),
                new ParticipacaoDTO(2L, "Bruno", 3, 4, 7, "2026-09")));

        mockMvc.perform(get("/api/forum/engajamento").param("mes", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeAluno").value("Ana"))
                .andExpect(jsonPath("$[1].engajamento").value(7));
    }

    @Test
    @DisplayName("POST /api/forum/encerrar-mes devolve o aluno premiado")
    void deveEncerrarOMesEPremiar() throws Exception {
        given(forumService.encerrarMes("2026-09"))
                .willReturn(PremiacaoDTO.premiado("2026-09", "Ana", 15, 1));

        mockMvc.perform(post("/api/forum/encerrar-mes").param("mes", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houvePremiacao").value(true))
                .andExpect(jsonPath("$.alunoPremiado").value("Ana"))
                .andExpect(jsonPath("$.cursosGanhos").value(1));
    }

    @Test
    @DisplayName("Mes sem participacao devolve 200 sem premiacao")
    void deveEncerrarMesSemPremiacao() throws Exception {
        given(forumService.encerrarMes("2026-07"))
                .willReturn(PremiacaoDTO.semPremiacao("2026-07"));

        mockMvc.perform(post("/api/forum/encerrar-mes").param("mes", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houvePremiacao").value(false))
                .andExpect(jsonPath("$.alunoPremiado").doesNotExist());
    }

    @Test
    @DisplayName("Violacao de regra do dominio vira 400 com o corpo de erro padrao")
    void regraDeNegocioVioladaDeve400() throws Exception {
        willThrow(new IllegalArgumentException("Nome do aluno e obrigatorio"))
                .given(forumService).cadastrarAluno(any(NovoAlunoDTO.class));

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Nome do aluno e obrigatorio"));
    }

    @Test
    @DisplayName("Aluno inexistente vira 404 com o corpo de erro padrao")
    void alunoInexistenteDeve404() throws Exception {
        willThrow(new RecursoNaoEncontradoException("Aluno nao encontrado: Fulano"))
                .given(forumService).registrarParticipacao(any(NovaParticipacaoDTO.class));

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
        given(forumService.listarEngajamentoDoMes(eq("setembro")))
                .willThrow(new IllegalArgumentException(
                        "Mes de referencia deve estar no formato AAAA-MM. Recebido: setembro"));

        mockMvc.perform(get("/api/forum/engajamento").param("mes", "setembro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
