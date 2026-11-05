package com.example.grupo12_praticaatdd.recomendacao.adaptador.saida.ia;

import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacao;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.MotorDeRecomendacaoIndisponivel;
import com.example.grupo12_praticaatdd.recomendacao.aplicacao.porta.saida.SugestoesDoMotor;
import com.example.grupo12_praticaatdd.recomendacao.dominio.CursoRecomendado;
import com.example.grupo12_praticaatdd.recomendacao.dominio.PerfilDeAprendizagem;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Adapter de saida: chama o servico Python (FastAPI) por REST/JSON.
 *
 * Toda falha de comunicacao (conexao recusada, timeout, 4xx/5xx, corpo invalido)
 * e traduzida para {@link MotorDeRecomendacaoIndisponivel}. Os timeouts ficam no
 * {@link RestClient} configurado em {@link RecomendacaoIaConfig}.
 */
public class MotorDeRecomendacaoHttp implements MotorDeRecomendacao {

    private final RestClient restClient;

    public MotorDeRecomendacaoHttp(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public SugestoesDoMotor recomendar(PerfilDeAprendizagem perfil) {
        RespostaIa resposta;
        try {
            resposta = restClient.post()
                    .uri("/recomendacoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PedidoIa(perfil.aluno(), perfil.cursosGanhos(),
                            perfil.topicosEscritos(), perfil.comentariosAjuda()))
                    .retrieve()
                    .body(RespostaIa.class);
        } catch (RestClientException falha) {
            throw new MotorDeRecomendacaoIndisponivel("falha ao chamar a IA: " + falha.getMessage(), falha);
        }
        if (resposta == null || resposta.cursos() == null) {
            throw new MotorDeRecomendacaoIndisponivel("resposta vazia da IA", null);
        }
        return new SugestoesDoMotor(resposta.versaoModelo(), resposta.cursos().stream()
                .map(curso -> new CursoRecomendado(curso.titulo(), curso.motivo()))
                .toList());
    }

    /** Corpo enviado ao FastAPI (contrato do servico Python). */
    record PedidoIa(String aluno, int cursosGanhos, int topicosEscritos, int comentariosAjuda) {
    }

    /** Corpo recebido do FastAPI. */
    record RespostaIa(String aluno, String versaoModelo, List<CursoIa> cursos) {
    }

    record CursoIa(String titulo, String motivo, double pontuacao) {
    }
}
