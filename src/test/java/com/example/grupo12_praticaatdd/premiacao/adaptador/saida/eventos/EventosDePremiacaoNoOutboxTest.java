package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.eventos;

import com.example.grupo12_praticaatdd.eventos.aplicacao.RegistroDeEventos;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventosDePremiacaoNoOutboxTest {

    @Test
    @DisplayName("Registra o evento premiacao.concedida com o payload JSON do contrato")
    void registraEventoNoOutbox() {
        RegistroDeEventos registro = mock(RegistroDeEventos.class);

        new EventosDePremiacaoNoOutbox(registro).premiacaoConcedida(3L, new ResultadoPremiacao("2026-09", "Carla", 20, 1));

        verify(registro).registrar("PremiacaoConcedida", "premiacao.concedida",
                "{\"tipo\":\"PremiacaoConcedida\",\"alunoId\":3,\"aluno\":\"Carla\",\"mesReferencia\":\"2026-09\","
                        + "\"engajamento\":20,\"cursosGanhos\":1}");
    }

    @Test
    @DisplayName("Aspas e barras no nome do aluno nao quebram o JSON")
    void escapaCaracteresEspeciais() {
        String json = EventosDePremiacaoNoOutbox.json(1L, new ResultadoPremiacao("2026-09", "Ana \"Bia\" \\ X", 5, 1));

        assertEquals("{\"tipo\":\"PremiacaoConcedida\",\"alunoId\":1,\"aluno\":\"Ana \\\"Bia\\\" \\\\ X\","
                + "\"mesReferencia\":\"2026-09\",\"engajamento\":5,\"cursosGanhos\":1}", json);
    }
}
