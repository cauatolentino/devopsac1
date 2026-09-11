package com.example.grupo12_praticaatdd.controller;

import com.example.grupo12_praticaatdd.dto.AlunoDTO;
import com.example.grupo12_praticaatdd.dto.NovaParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.NovoAlunoDTO;
import com.example.grupo12_praticaatdd.dto.ParticipacaoDTO;
import com.example.grupo12_praticaatdd.dto.PremiacaoDTO;
import com.example.grupo12_praticaatdd.service.ForumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST da US1 - premiacao por engajamento no forum.
 */
@RestController
@RequestMapping("/api")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/alunos")
    public ResponseEntity<AlunoDTO> cadastrarAluno(@RequestBody NovoAlunoDTO novoAluno) {
        return ResponseEntity.status(HttpStatus.CREATED).body(forumService.cadastrarAluno(novoAluno));
    }

    @GetMapping("/alunos")
    public List<AlunoDTO> listarAlunos() {
        return forumService.listarAlunos();
    }

    @PostMapping("/participacoes")
    public ResponseEntity<ParticipacaoDTO> registrarParticipacao(
            @RequestBody NovaParticipacaoDTO novaParticipacao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(forumService.registrarParticipacao(novaParticipacao));
    }

    @GetMapping("/forum/engajamento")
    public List<ParticipacaoDTO> engajamentoDoMes(@RequestParam String mes) {
        return forumService.listarEngajamentoDoMes(mes);
    }

    @PostMapping("/forum/encerrar-mes")
    public PremiacaoDTO encerrarMes(@RequestParam String mes) {
        return forumService.encerrarMes(mes);
    }
}
