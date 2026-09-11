package com.example.grupo12_praticaatdd.controller;

import com.example.grupo12_praticaatdd.service.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Traduz as excecoes do dominio e do service para respostas HTTP.
 *
 * As validacoes do dominio (nome em branco, quantidade negativa, mes invalido)
 * chegam aqui como IllegalArgumentException e viram 400.
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDTO> regraDeNegocioViolada(IllegalArgumentException excecao) {
        return resposta(HttpStatus.BAD_REQUEST, excecao.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> recursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return resposta(HttpStatus.NOT_FOUND, excecao.getMessage());
    }

    private ResponseEntity<ErroDTO> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(new ErroDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem));
    }
}
