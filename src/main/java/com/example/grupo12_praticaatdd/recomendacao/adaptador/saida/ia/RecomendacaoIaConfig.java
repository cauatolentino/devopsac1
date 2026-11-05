package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;

/**
 * Liga a porta {@link MotorDeRecomendacao} ao servico Python.
 *
 * Duas linhas de defesa:
 *  1. timeouts de conexao/leitura (Desafio 3): um modelo lento nao segura a thread;
 *  2. disjuntor (Desafio 6): com a IA fora, para de chamar por um tempo - cobre o que o
 *     timeout nao cobre (ex.: resolucao de DNS de um container parado, medida em 4-8 s).
 */
@Configuration
public class RecomendacaoIaConfig {

    @Bean
    MotorDeRecomendacao motorDeRecomendacao(
            @Value("${ia.recomendacao.url:http://localhost:8000}") String url,
            @Value("${ia.recomendacao.timeout-conexao-ms:500}") long timeoutConexaoMs,
            @Value("${ia.recomendacao.timeout-leitura-ms:1500}") long timeoutLeituraMs,
            @Value("${ia.recomendacao.disjuntor.falhas-para-abrir:2}") int falhasParaAbrir,
            @Value("${ia.recomendacao.disjuntor.aberto-ms:30000}") long abertoMs,
            @Value("${ia.recomendacao.prazo-total-ms:2000}") long prazoTotalMs,
            MeterRegistry metricas) {
        // disjuntor -> prazo total -> HTTP (timeouts de conexao/leitura)
        MotorDeRecomendacao comPrazo = new PrazoTotalDoMotor(motorHttp(url, timeoutConexaoMs, timeoutLeituraMs),
                Duration.ofMillis(prazoTotalMs), 8);
        DisjuntorDoMotor disjuntor = new DisjuntorDoMotor(comPrazo,
                falhasParaAbrir, Duration.ofMillis(abertoMs), Clock.systemUTC());
        Gauge.builder("gamificacao.recomendacao.disjuntor.aberto", disjuntor, d -> d.aberto() ? 1 : 0)
                .description("1 = disjuntor aberto (IA nao esta sendo chamada)")
                .register(metricas);
        return disjuntor;
    }

    MotorDeRecomendacaoHttp motorHttp(String url, long timeoutConexaoMs, long timeoutLeituraMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutConexaoMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutLeituraMs));
        return new MotorDeRecomendacaoHttp(RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build());
    }
}
