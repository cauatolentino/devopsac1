package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.modulos;

import com.example.grupo12_praticaatdd.aluno.aplicacao.AlunoService;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.CreditoDeCursos;
import org.springframework.stereotype.Component;

/**
 * Adapter de saida: atende a porta {@link CreditoDeCursos} pelo contrato publico do modulo aluno.
 */
@Component
public class CursosDoAlunoAdapter implements CreditoDeCursos {

    private final AlunoService alunoService;

    public CursosDoAlunoAdapter(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @Override
    public void creditar(Long alunoId, int totalDeCursosGanhos) {
        alunoService.atualizarCursosGanhos(alunoId, totalDeCursosGanhos);
    }
}
