package com.example.grupo12_praticaatdd.dto;

/**
 * Resultado do encerramento do mes: quem foi premiado com 1 curso (US1).
 */
public record PremiacaoDTO(String mesReferencia,
                           boolean houvePremiacao,
                           String alunoPremiado,
                           int engajamento,
                           int cursosGanhos,
                           String mensagem) {

    public static PremiacaoDTO semPremiacao(String mesReferencia) {
        return new PremiacaoDTO(mesReferencia, false, null, 0, 0,
                "Nenhuma participacao registrada no forum em " + mesReferencia
                        + ": nenhum aluno foi premiado.");
    }

    public static PremiacaoDTO premiado(String mesReferencia, String nome, int engajamento,
                                        int cursosGanhos) {
        return new PremiacaoDTO(mesReferencia, true, nome, engajamento, cursosGanhos,
                nome + " foi o aluno de maior engajamento em " + mesReferencia
                        + " e ganhou 1 curso.");
    }
}
