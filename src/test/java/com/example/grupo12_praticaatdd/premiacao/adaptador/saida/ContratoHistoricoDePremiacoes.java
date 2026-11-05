package com.example.grupo12_praticaatdd.premiacao.adaptador.saida;

import com.example.grupo12_praticaatdd.premiacao.aplicacao.ResultadoPremiacao;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.HistoricoDePremiacoes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato da porta {@link HistoricoDePremiacoes}.
 *
 * Os MESMOS testes rodam contra cada adapter (memoria e JPA). Se um adapter
 * novo passar aqui, ele pode substituir o outro sem mexer no nucleo.
 *
 * {@code @Transactional} aqui e proposital: no Spring 7 a transacao (e o rollback)
 * de teste e decidida pela classe que DECLARA o metodo de teste. Sem isso, os
 * testes herdados pelo adapter JPA gravariam de verdade e um vazaria no outro.
 * No teste em memoria (sem Spring) a anotacao nao tem efeito.
 */
@Transactional
public abstract class ContratoHistoricoDePremiacoes {

    protected abstract HistoricoDePremiacoes historico();

    @Test
    @DisplayName("Mes nunca premiado nao tem registro")
    public void mesSemPremiacaoNaoTemRegistro() {
        assertTrue(historico().buscar("2026-01").isEmpty());
    }

    @Test
    @DisplayName("Premiacao registrada e recuperada com os mesmos dados")
    public void registraERecuperaPremiacao() {
        historico().registrar(new ResultadoPremiacao("2026-09", "Carla", 20, 1));

        ResultadoPremiacao recuperada = historico().buscar("2026-09").orElseThrow();

        assertEquals(new ResultadoPremiacao("2026-09", "Carla", 20, 1), recuperada);
        assertTrue(historico().buscar("2026-10").isEmpty());
    }

    @Test
    @DisplayName("O mesmo mes nao pode ser registrado duas vezes")
    public void naoRegistraOMesmoMesDuasVezes() {
        historico().registrar(new ResultadoPremiacao("2026-09", "Carla", 20, 1));

        assertThrows(RuntimeException.class,
                () -> historico().registrar(new ResultadoPremiacao("2026-09", "Ana", 15, 1)));
    }
}
