package com.example.grupo12_praticaatdd.premiacao.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.entrada.EncerrarMes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter de entrada HTTP do modulo premiacao (RF02 - conquistas).
 *
 * Depende so da porta {@link EncerrarMes}, nao da implementacao do caso de uso.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Premiacao", description = "Modulo premiacao - US1: premia o aluno de maior engajamento do mes")
public class PremiacaoController {

    private final EncerrarMes encerrarMes;

    public PremiacaoController(EncerrarMes encerrarMes) {
        this.encerrarMes = encerrarMes;
    }

    @PostMapping("/forum/encerrar-mes")
    @Operation(summary = "Encerra o mes e premia com 1 curso o aluno de maior engajamento",
            description = "Se nao houve nenhuma participacao no mes, ninguem e premiado. "
                    + "Encerrar de novo um mes ja premiado devolve a mesma premiacao, sem creditar outro curso.")
    public PremiacaoDTO encerrarMes(
            @Parameter(description = "Mes de referencia no formato AAAA-MM", example = "2026-09")
            @RequestParam String mes) {
        return PremiacaoDTO.de(encerrarMes.encerrarMes(mes));
    }
}
