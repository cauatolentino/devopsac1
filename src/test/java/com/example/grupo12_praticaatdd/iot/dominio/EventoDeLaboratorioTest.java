package com.example.grupo12_praticaatdd.iot.dominio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EventoDeLaboratorioTest {

    private static final Instant AGORA = Instant.parse("2026-11-04T13:00:00Z");

    @Test
    @DisplayName("Campos obrigatorios do evento de laboratorio sao validados no dominio")
    void camposObrigatorios() {
        TipoEventoLaboratorio presenca = TipoEventoLaboratorio.PRESENCA;
        assertThrows(IllegalArgumentException.class, () -> new EventoDeLaboratorio(null, "l", "d", presenca, "a", null, AGORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoDeLaboratorio("e", " ", "d", presenca, "a", null, AGORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoDeLaboratorio("e", "l", "", presenca, "a", null, AGORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoDeLaboratorio("e", "l", "d", presenca, null, null, AGORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoDeLaboratorio("e", "l", "d", null, "a", null, AGORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoDeLaboratorio("e", "l", "d", presenca, "a", null, null));
    }
}
