package com.example.grupo12_praticaatdd.service;

/**
 * Lancada quando um recurso referenciado pela requisicao nao existe no banco.
 * Traduzida para HTTP 404 pelo tratador global de excecoes.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
