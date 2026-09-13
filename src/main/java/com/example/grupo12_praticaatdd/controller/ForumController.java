package com.example.grupo12_praticaatdd.controller;

import com.example.grupo12_praticaatdd.dto.AlunoDTO;
import com.example.grupo12_praticaatdd.dto.NovaParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.NovoAlunoDTO;
import com.example.grupo12_praticaatdd.dto.ParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.PremiacaoDTO;
import com.example.grupo12_praticaatdd.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST da US1 - premiacao por engajamento no forum.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Forum", description = "US1 - premiacao do aluno com maior engajamento no forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/alunos")
    @Operation(summary = "Cadastra um aluno assinante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno cadastrado"),
            @ApiResponse(responseCode = "400", description = "Nome em branco ou aluno ja cadastrado")
    })
    public ResponseEntity<AlunoDTO> cadastrarAluno(@RequestBody NovoAlunoDTO novoAluno) {
        return ResponseEntity.status(HttpStatus.CREATED).body(forumService.cadastrarAluno(novoAluno));
    }

    @GetMapping("/alunos")
    @Operation(summary = "Lista os alunos e quantos cursos cada um ja ganhou")
    public List<AlunoDTO> listarAlunos() {
        return forumService.listarAlunos();
    }

    @PostMapping("/participacoes")
    @Operation(summary = "Registra a participacao de um aluno no forum no mes")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Participacao registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade negativa ou mes invalido"),
            @ApiResponse(responseCode = "404", description = "Aluno nao cadastrado")
    })
    public ResponseEntity<ParticipacaoDTO> registrarParticipacao(
            @RequestBody NovaParticipacaoDTO novaParticipacao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(forumService.registrarParticipacao(novaParticipacao));
    }

    @GetMapping("/forum/engajamento")
    @Operation(summary = "Ranking de engajamento do mes, do maior para o menor")
    public List<ParticipacaoDTO> engajamentoDoMes(
            @Parameter(description = "Mes de referencia no formato AAAA-MM", example = "2026-09")
            @RequestParam String mes) {
        return forumService.listarEngajamentoDoMes(mes);
    }

    @PostMapping("/forum/encerrar-mes")
    @Operation(summary = "Encerra o mes e premia com 1 curso o aluno de maior engajamento",
            description = "Se nao houve nenhuma participacao no mes, ninguem e premiado.")
    public PremiacaoDTO encerrarMes(
            @Parameter(description = "Mes de referencia no formato AAAA-MM", example = "2026-09")
            @RequestParam String mes) {
        return forumService.encerrarMes(mes);
    }
}
