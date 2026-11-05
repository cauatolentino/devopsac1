package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Circuit breaker (disjuntor) na frente do adapter HTTP da IA - B1 / Desafio 6.
 *
 * Por que existe: o ADR-003 adiou o disjuntor "ate as metricas justificarem". No HOMOL,
 * o painel de latencia mostrou chamadas de 4 a 8 s com a IA parada: a RESOLUCAO DE DNS
 * do nome de um container parado nao e coberta pelos timeouts de conexao/leitura.
 *
 * Estados:
 *   FECHADO     chamadas passam; N falhas SEGUIDAS -> ABERTO
 *   ABERTO      falha na hora, sem rede nem DNS, durante {@code tempoAberto}
 *   MEIO-ABERTO passado o tempo, deixa UMA chamada de prova: sucesso fecha, falha reabre
 *
 * Fica no adapter (decorator da porta): o caso de uso nao muda e continua so vendo
 * {@link MotorDeRecomendacaoIndisponivel}.
 */
public class DisjuntorDoMotor implements MotorDeRecomendacao {

    private final MotorDeRecomendacao motor;
    private final int falhasParaAbrir;
    private final Duration tempoAberto;
    private final Clock relogio;

    private int falhasSeguidas;
    private Instant abertoAte;

    public DisjuntorDoMotor(MotorDeRecomendacao motor, int falhasParaAbrir, Duration tempoAberto, Clock relogio) {
        this.motor = motor;
        this.falhasParaAbrir = falhasParaAbrir;
        this.tempoAberto = tempoAberto;
        this.relogio = relogio;
    }

    @Override
    public SugestoesDoMotor recomendar(PerfilDeAprendizagem perfil) {
        synchronized (this) {
            if (abertoAte != null && relogio.instant().isBefore(abertoAte)) {
                throw new MotorDeRecomendacaoIndisponivel("disjuntor aberto: IA nao e chamada ate " + abertoAte, null);
            }
        }
        try {
            SugestoesDoMotor sugestoes = motor.recomendar(perfil);
            registrarSucesso();
            return sugestoes;
        } catch (MotorDeRecomendacaoIndisponivel falha) {
            registrarFalha();
            throw falha;
        }
    }

    public synchronized boolean aberto() {
        return abertoAte != null && relogio.instant().isBefore(abertoAte);
    }

    private synchronized void registrarSucesso() {
        falhasSeguidas = 0;
        abertoAte = null;
    }

    private synchronized void registrarFalha() {
        falhasSeguidas++;
        if (falhasSeguidas >= falhasParaAbrir) {
            abertoAte = relogio.instant().plus(tempoAberto);
        }
    }
}
