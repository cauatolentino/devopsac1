package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do adapter HTTP contra um servidor HTTP de verdade (HttpServer do JDK)
 * que imita o servico FastAPI: respondendo, lento, com erro e fora do ar.
 */
class MotorDeRecomendacaoHttpTest {

    private static final PerfilDeAprendizagem CARLA = new PerfilDeAprendizagem("Carla", 1, 12, 8);
    private static final int TIMEOUT_MS = 300;

    private HttpServer servidor;
    private final AtomicReference<String> corpoRecebido = new AtomicReference<>();

    @AfterEach
    void desligar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    @DisplayName("IA respondendo: envia o perfil no contrato do FastAPI e traduz a resposta")
    void iaRespondendo() throws IOException {
        MotorDeRecomendacao motor = motorApontandoPara(iaFalsa(200, 0, """
                {"aluno":"Carla","versaoModelo":"heuristico-v1","cursos":[
                  {"titulo":"Escrita tecnica","motivo":"perfil autor","pontuacao":0.84}]}"""));

        SugestoesDoMotor sugestoes = motor.recomendar(CARLA);

        assertEquals("heuristico-v1", sugestoes.versaoModelo());
        assertEquals(new CursoRecomendado("Escrita tecnica", "perfil autor"), sugestoes.cursos().get(0));
        assertTrue(corpoRecebido.get().contains("\"topicosEscritos\":12"));
        assertTrue(corpoRecebido.get().contains("\"comentariosAjuda\":8"));
    }

    @Test
    @DisplayName("IA com erro 503 vira MotorDeRecomendacaoIndisponivel")
    void iaComErro() throws IOException {
        MotorDeRecomendacao motor = motorApontandoPara(iaFalsa(503, 0, "{\"detail\":\"fora\"}"));

        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> motor.recomendar(CARLA));
    }

    @Test
    @DisplayName("IA mais lenta que o timeout de leitura vira indisponivel (sem segurar a thread)")
    void iaLenta() throws IOException {
        MotorDeRecomendacao motor = motorApontandoPara(iaFalsa(200, TIMEOUT_MS * 4, "{}"));

        long inicio = System.nanoTime();
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> motor.recomendar(CARLA));
        long decorridoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertTrue(decorridoMs < TIMEOUT_MS * 3L, "deveria desistir perto do timeout, levou " + decorridoMs + " ms");
    }

    @Test
    @DisplayName("IA fora do ar (conexao recusada) vira indisponivel")
    void iaForaDoAr() throws IOException {
        int portaLivre;
        try (ServerSocket socket = new ServerSocket(0)) {
            portaLivre = socket.getLocalPort();
        }
        MotorDeRecomendacao motor = motorApontandoPara(portaLivre);

        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> motor.recomendar(CARLA));
    }

    @Test
    @DisplayName("Resposta sem corpo ou sem a lista de cursos vira indisponivel")
    void respostaInvalida() throws IOException {
        MotorDeRecomendacao semCorpo = motorApontandoPara(iaFalsa(200, 0, ""));
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> semCorpo.recomendar(CARLA));
        servidor.stop(0);

        MotorDeRecomendacao semCursos = motorApontandoPara(iaFalsa(200, 0, "{\"aluno\":\"Carla\",\"versaoModelo\":\"v\"}"));
        assertThrows(MotorDeRecomendacaoIndisponivel.class, () -> semCursos.recomendar(CARLA));
    }

    private MotorDeRecomendacao motorApontandoPara(int porta) {
        return new RecomendacaoIaConfig().motorHttp("http://localhost:" + porta, TIMEOUT_MS, TIMEOUT_MS);
    }

    private int iaFalsa(int status, long atrasoMs, String corpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/recomendacoes", troca -> {
            corpoRecebido.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            dormir(atrasoMs);
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream saida = troca.getResponseBody()) {
                saida.write(bytes);
            }
        });
        servidor.start();
        return servidor.getAddress().getPort();
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
