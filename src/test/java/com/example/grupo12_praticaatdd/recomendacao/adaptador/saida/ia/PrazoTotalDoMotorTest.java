package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrazoTotalDoMotorTest {

    private static final PerfilDeAprendizagem PERFIL = new PerfilDeAprendizagem("Carla", 1, 12, 8);
    private static final SugestoesDoMotor OK = new SugestoesDoMotor("v1", List.of());

    @Test
    @DisplayName("Dentro do prazo: devolve a resposta da IA")
    void dentroDoPrazo() {
        assertEquals(OK, new PrazoTotalDoMotor(perfil -> OK, Duration.ofSeconds(1), 1).recomendar(PERFIL));
    }

    @Test
    @DisplayName("Etapa lenta que os timeouts nao cobrem (ex.: DNS): desiste no prazo total")
    void estouraOPrazo() {
        PrazoTotalDoMotor motor = new PrazoTotalDoMotor(perfil -> {
            dormir(3000);
            return OK;
        }, Duration.ofMillis(200), 1);

        long inicio = System.nanoTime();
        MotorDeRecomendacaoIndisponivel erro = assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> motor.recomendar(PERFIL));
        long decorridoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertTrue(decorridoMs < 1000, "deveria desistir perto de 200 ms, levou " + decorridoMs + " ms");
        assertTrue(erro.getMessage().contains("prazo total"));
    }

    @Test
    @DisplayName("Falha conhecida da IA passa adiante; falha inesperada vira indisponivel")
    void traducaoDeFalhas() {
        MotorDeRecomendacaoIndisponivel conhecida = new MotorDeRecomendacaoIndisponivel("503", null);
        assertEquals(conhecida, assertThrows(MotorDeRecomendacaoIndisponivel.class,
                () -> new PrazoTotalDoMotor(perfil -> { throw conhecida; }, Duration.ofSeconds(1), 1).recomendar(PERFIL)));

        MotorDeRecomendacaoIndisponivel inesperada = assertThrows(MotorDeRecomendacaoIndisponivel.class,
                () -> new PrazoTotalDoMotor(perfil -> { throw new IllegalStateException("bug"); }, Duration.ofSeconds(1), 1)
                        .recomendar(PERFIL));
        assertTrue(inesperada.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("Thread chamadora interrompida: vira indisponivel e preserva a interrupcao")
    void interrompida() {
        PrazoTotalDoMotor motor = new PrazoTotalDoMotor(perfil -> {
            dormir(3000);
            return OK;
        }, Duration.ofSeconds(5), 1);

        Thread.currentThread().interrupt();
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> motor.recomendar(PERFIL));
        assertTrue(Thread.interrupted(), "a flag de interrupcao deve ser restaurada");
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
