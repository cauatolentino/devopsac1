package com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;

/**
 * Porta de saida (Desafio 4): avisa o resto do ecossistema que uma premiacao
 * foi concedida (notificacao ao aluno, analytics).
 *
 * Contrato: registrar o fato e retornar rapido. A ENTREGA aos consumidores e
 * assincrona e nao pode atrasar nem impedir a premiacao.
 */
public interface EventosDePremiacao {

    void premiacaoConcedida(Long alunoId, ResultadoPremiacao premiacao);
}
