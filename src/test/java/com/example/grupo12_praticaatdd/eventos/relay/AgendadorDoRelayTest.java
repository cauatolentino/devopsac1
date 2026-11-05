package com.example.grupo12_praticaatdd.eventos.relay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AgendadorDoRelayTest {

    @Test
    @DisplayName("Cada disparo do agendador executa um ciclo do relay")
    void disparaRelay() {
        RelayDoOutbox relay = mock(RelayDoOutbox.class);

        new AgendadorDoRelay(relay).executar();

        verify(relay).publicarPendentes();
    }
}
