package com.example.grupo12_praticaatdd.compartilhado.web;

import java.time.LocalDateTime;

/**
 * Corpo padrao das respostas de erro da API.
 */
public record ErroDTO(LocalDateTime momento, int status, String erro, String mensagem) {
}
