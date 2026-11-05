package com.example.grupo12_praticaatdd.iot.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.iot.aplicacao.EventosDeLaboratorioService;
import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import com.example.grupo12_praticaatdd.iot.dominio.TipoEventoLaboratorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaboratorioController.class)
class LaboratorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventosDeLaboratorioService service;

    @Test
    @DisplayName("GET /api/laboratorio/eventos devolve os eventos IoT do aluno")
    void eventosDoAluno() throws Exception {
        given(service.eventosDoAluno("Carla")).willReturn(List.of(new EventoDeLaboratorio("e1", "lab-redes",
                "leitor-07", TipoEventoLaboratorio.PRESENCA, "Carla", null, Instant.parse("2026-11-04T13:00:00Z"))));

        mockMvc.perform(get("/api/laboratorio/eventos").param("aluno", "Carla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("PRESENCA"))
                .andExpect(jsonPath("$[0].dispositivo").value("leitor-07"));
    }
}
