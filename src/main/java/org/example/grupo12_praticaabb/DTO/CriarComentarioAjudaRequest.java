package org.example.grupo12_praticaabb.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarComentarioAjudaRequest(
        @NotBlank(message = "Conteúdo é obrigatório") @Size(max = 500) String conteudo) {}
