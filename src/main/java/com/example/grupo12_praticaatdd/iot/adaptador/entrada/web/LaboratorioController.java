package com.example.grupo12_praticaatdd.iot.adaptador.entrada.web;

import com.example.grupo12_praticaatdd.iot.aplicacao.EventosDeLaboratorioService;
import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta dos eventos de laboratorio que entraram pela integracao IoT (RF06).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Laboratorio (IoT)", description = "Modulo iot - RF06: eventos de presenca e atividades vindos de dispositivos")
public class LaboratorioController {

    private final EventosDeLaboratorioService service;

    public LaboratorioController(EventosDeLaboratorioService service) {
        this.service = service;
    }

    @GetMapping("/laboratorio/eventos")
    @Operation(summary = "Eventos de laboratorio de um aluno, em ordem cronologica")
    public List<EventoDeLaboratorio> eventosDoAluno(
            @Parameter(description = "Nome do aluno", example = "Carla") @RequestParam String aluno) {
        return service.eventosDoAluno(aluno);
    }
}
