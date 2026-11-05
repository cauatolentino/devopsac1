package com.example.grupo12_praticaatdd.aluno.web;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST do modulo {@code aluno}.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Alunos", description = "Modulo aluno - cadastro e consulta de alunos assinantes")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping("/alunos")
    @Operation(summary = "Cadastra um aluno assinante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno cadastrado"),
            @ApiResponse(responseCode = "400", description = "Nome em branco ou aluno ja cadastrado")
    })
    public ResponseEntity<AlunoDTO> cadastrarAluno(@RequestBody NovoAlunoDTO novoAluno) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AlunoDTO.de(alunoService.cadastrar(novoAluno.nome())));
    }

    @GetMapping("/alunos")
    @Operation(summary = "Lista os alunos e quantos cursos cada um ja ganhou")
    public List<AlunoDTO> listarAlunos() {
        return alunoService.listar().stream()
                .map(AlunoDTO::de)
                .toList();
    }
}
