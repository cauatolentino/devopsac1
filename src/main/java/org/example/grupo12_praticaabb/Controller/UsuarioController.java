package org.example.grupo12_praticaabb.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.grupo12_praticaabb.DTO.*;
import org.example.grupo12_praticaabb.Service.EngajamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/alunos")
@Tag(name = "Alunos", description = "Cadastro e gamificação de alunos")
public class UsuarioController {
    private final EngajamentoService service;

    public UsuarioController(EngajamentoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um aluno")
    public UsuarioResponse criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return service.criarUsuario(request.nome(), request.email());
    }

    @GetMapping
    @Operation(summary = "Lista os alunos e seus indicadores")
    public List<UsuarioResponse> listar() { return service.listarUsuarios(); }

    @PostMapping("/{id}/topicos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um tópico criado pelo aluno")
    public void adicionarTopico(@PathVariable Long id, @Valid @RequestBody CriarTopicoRequest request) {
        service.adicionarTopico(id, request.titulo());
    }

    @PostMapping("/{id}/comentarios-ajuda")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um comentário de ajuda do aluno")
    public void adicionarComentario(@PathVariable Long id, @Valid @RequestBody CriarComentarioAjudaRequest request) {
        service.adicionarComentarioAjuda(id, request.conteudo());
    }

    @GetMapping("/{id}/engajamento")
    @Operation(summary = "Calcula o engajamento: tópicos + comentários de ajuda")
    public EngajamentoResponse engajamento(@PathVariable Long id) {
        return service.calcularEngajamento(id);
    }
}
