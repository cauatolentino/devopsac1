package com.example.grupo12_praticaatdd.engajamento.web;

import com.example.grupo12_praticaatdd.engajamento.aplicacao.EngajamentoService;
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
 * Endpoints REST do modulo {@code engajamento} (RF01 - ranking do forum).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Engajamento", description = "Modulo engajamento - participacoes no forum e ranking do mes")
public class EngajamentoController {

    private final EngajamentoService engajamentoService;

    public EngajamentoController(EngajamentoService engajamentoService) {
        this.engajamentoService = engajamentoService;
    }

    @PostMapping("/participacoes")
    @Operation(summary = "Registra a participacao de um aluno no forum no mes")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Participacao registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade negativa ou mes invalido"),
            @ApiResponse(responseCode = "404", description = "Aluno nao cadastrado")
    })
    public ResponseEntity<ParticipacaoDTO> registrarParticipacao(
            @RequestBody NovaParticipacaoDTO nova) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ParticipacaoDTO.de(
                engajamentoService.registrar(nova.nomeAluno(), nova.topicosEscritos(),
                        nova.comentariosAjuda(), nova.mesReferencia())));
    }

    @GetMapping("/forum/engajamento")
    @Operation(summary = "Ranking de engajamento do mes, do maior para o menor")
    public List<ParticipacaoDTO> engajamentoDoMes(
            @Parameter(description = "Mes de referencia no formato AAAA-MM", example = "2026-09")
            @RequestParam String mes) {
        return engajamentoService.rankingDoMes(mes).stream()
                .map(ParticipacaoDTO::de)
                .toList();
    }
}
