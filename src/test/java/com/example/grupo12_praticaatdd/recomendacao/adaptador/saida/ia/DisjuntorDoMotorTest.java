package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Disjuntor com relogio controlado: nada de sleep no teste.
 */
class DisjuntorDoMotorTest {

    private static final PerfilDeAprendizagem PERFIL = new PerfilDeAprendizagem("Carla", 1, 12, 8);
    private static final SugestoesDoMotor OK = new SugestoesDoMotor("v1", List.of());

    private final RelogioFalso relogio = new RelogioFalso();
    private int chamadasAIa;
    private boolean iaNoAr = true;

    private final MotorDeRecomendacao iaFalsa = perfil -> {
        chamadasAIa++;
        if (!iaNoAr) {
            throw new MotorDeRecomendacaoIndisponivel("fora", null);
        }
        return OK;
    };

    private final DisjuntorDoMotor disjuntor = new DisjuntorDoMotor(iaFalsa, 2, Duration.ofSeconds(30), relogio);

    @Test
    @DisplayName("Fechado: chamadas passam; uma falha isolada nao abre (limite = 2 seguidas)")
    void fechadoComFalhaIsolada() {
        assertEquals(OK, disjuntor.recomendar(PERFIL));
        iaNoAr = false;
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));
        assertFalse(disjuntor.aberto());
        iaNoAr = true;
        assertEquals(OK, disjuntor.recomendar(PERFIL), "um sucesso zera a contagem de falhas");
        assertEquals(3, chamadasAIa);
    }

    @Test
    @DisplayName("2 falhas seguidas abrem: enquanto aberto, falha NA HORA sem chamar a IA")
    void abreENaoChamaAIa() {
        iaNoAr = false;
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));
        assertTrue(disjuntor.aberto());

        for (int i = 0; i < 10; i++) {
            assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));
        }
        assertEquals(2, chamadasAIa, "com o disjuntor aberto a IA (e o DNS dela) nao sao tocados");
    }

    @Test
    @DisplayName("Meio-aberto: passado o tempo, uma prova; sucesso fecha, falha reabre")
    void meioAberto() {
        iaNoAr = false;
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));

        relogio.avancar(Duration.ofSeconds(31));
        assertFalse(disjuntor.aberto());
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> disjuntor.recomendar(PERFIL));
        assertTrue(disjuntor.aberto(), "a prova falhou: reabre");
        assertEquals(3, chamadasAIa);

        relogio.avancar(Duration.ofSeconds(31));
        iaNoAr = true;
        assertEquals(OK, disjuntor.recomendar(PERFIL));
        assertFalse(disjuntor.aberto(), "a prova passou: fecha");
    }

    @Test
    @DisplayName("Bean de producao: disjuntor na frente do HTTP + metrica 0/1 do estado")
    void beanComMetrica() {
        SimpleMeterRegistry metricas = new SimpleMeterRegistry();
        MotorDeRecomendacao motor = new RecomendacaoIaConfig()
                .motorDeRecomendacao("http://localhost:1", 100, 100, 1, 30_000, 2_000, metricas);

        assertTrue(motor instanceof DisjuntorDoMotor);
        assertEquals(0.0, metricas.get("gamificacao.recomendacao.disjuntor.aberto").gauge().value());
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> motor.recomendar(PERFIL));
        assertEquals(1.0, metricas.get("gamificacao.recomendacao.disjuntor.aberto").gauge().value());
    }

    /** Relogio que so anda quando o teste manda. */
    private static final class RelogioFalso extends Clock {
        private Instant agora = Instant.parse("2026-11-04T12:00:00Z");

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
