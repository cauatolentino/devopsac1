package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Prazo TOTAL por chamada a IA (B1 / Desafio 6).
 *
 * Os timeouts de conexao e leitura nao cobrem a resolucao de DNS: no HOMOL, com o
 * container da IA parado, a 1a chamada levou 8,2 s mesmo com o disjuntor. Este
 * decorator garante o orcamento de latencia do ADR-003 para QUALQUER etapa da chamada.
 *
 * A chamada roda num pool pequeno e limitado; se estourar o prazo, o Core responde
 * degradado na hora e a thread do pool termina sozinha quando o DNS/socket desistir.
 */
public class PrazoTotalDoMotor implements MotorDeRecomendacao {

    private final MotorDeRecomendacao motor;
    private final Duration prazo;
    private final ExecutorService executor;

    public PrazoTotalDoMotor(MotorDeRecomendacao motor, Duration prazo, int threads) {
        this.motor = motor;
        this.prazo = prazo;
        this.executor = Executors.newFixedThreadPool(threads, tarefa -> {
            Thread thread = new Thread(tarefa, "ia-recomendacao-chamada");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public SugestoesDoMotor recomendar(PerfilDeAprendizagem perfil) {
        CompletableFuture<SugestoesDoMotor> chamada = CompletableFuture.supplyAsync(() -> motor.recomendar(perfil), executor);
        try {
            return chamada.get(prazo.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException estourou) {
            chamada.cancel(true);
            throw new MotorDeRecomendacaoIndisponivel("prazo total de " + prazo.toMillis() + " ms excedido", estourou);
        } catch (ExecutionException falhou) {
            if (falhou.getCause() instanceof MotorDeRecomendacaoIndisponivel indisponivel) {
                throw indisponivel;
            }
            throw new MotorDeRecomendacaoIndisponivel("falha inesperada ao chamar a IA", falhou.getCause());
        } catch (InterruptedException interrompida) {
            Thread.currentThread().interrupt();
            throw new MotorDeRecomendacaoIndisponivel("chamada a IA interrompida", interrompida);
        }
    }
}
