package com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida;

import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;

/**
 * Porta de saida para o motor de recomendacao (servico Python de IA).
 *
 * Implementacoes devem lancar {@link MotorDeRecomendacaoIndisponivel} para
 * QUALQUER falha (fora do ar, lento, erro 5xx, resposta invalida): o Core
 * decide o comportamento degradado, nao o adapter.
 */
public interface MotorDeRecomendacao {

    SugestoesDoMotor recomendar(PerfilDeAprendizagem perfil);
}
