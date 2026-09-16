package org.example.grupo12_praticaabb.DTO;

import jakarta.validation.constraints.NotBlank;

public record CriarTopicoRequest(@NotBlank(message = "Título é obrigatório") String titulo) {}
