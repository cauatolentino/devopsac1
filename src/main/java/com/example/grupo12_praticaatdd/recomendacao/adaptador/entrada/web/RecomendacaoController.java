package com.example.grupo12_praticaatdd.recomendacao.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST do modulo recomendacao (RF03).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Recomendacao", description = "Modulo recomendacao - RF03: cursos sugeridos pela IA (servico Python)")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/recomendacoes")
    @Operation(summary = "Recomenda cursos para o aluno",
            description = "Se o servico de IA estiver fora do ar ou lento, responde 200 com o catalogo padrao "
                    + "e degradado=true.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendacoes (da IA ou do catalogo padrao)"),
            @ApiResponse(responseCode = "404", description = "Aluno nao cadastrado")
    })
    public RecomendacoesDTO recomendar(
            @Parameter(description = "Nome do aluno", example = "Carla") @RequestParam String aluno) {
        return RecomendacoesDTO.de(recomendacaoService.recomendar(aluno));
    }
}
