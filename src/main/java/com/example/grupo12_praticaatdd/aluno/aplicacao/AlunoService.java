package com.example.grupo12_praticaatdd.aluno.aplicacao;

import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import com.example.grupo12_praticaatdd.aluno.persistencia.AlunoEntity;
import com.example.grupo12_praticaatdd.aluno.persistencia.AlunoRepository;
import com.example.grupo12_praticaatdd.compartilhado.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrato publico do modulo {@code aluno}.
 *
 * Os modulos {@code engajamento} e {@code premiacao} so falam com alunos por
 * aqui: nunca acessam {@link AlunoRepository} nem {@link AlunoEntity}. Se um dia
 * o cadastro de alunos virar um servico separado, e esta classe que vira o
 * cliente HTTP, sem mexer nos outros modulos.
 */
@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public AlunoResumo cadastrar(String nome) {
        // valida o nome pelas regras do dominio antes de tocar no banco
        Aluno aluno = new Aluno(nome);

        if (alunoRepository.existsByNome(aluno.getNome())) {
            throw new IllegalArgumentException("Ja existe um aluno chamado " + aluno.getNome());
        }
        return AlunoResumo.de(alunoRepository.save(new AlunoEntity(aluno.getNome())));
    }

    @Transactional(readOnly = true)
    public List<AlunoResumo> listar() {
        return alunoRepository.findAll().stream()
                .map(AlunoResumo::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlunoResumo buscarPorNome(String nome) {
        return AlunoResumo.de(entidadePorNome(nome));
    }

    /**
     * Carrega os alunos como objetos de dominio (com os cursos ja ganhos),
     * indexados pelo id. Usado por outros modulos para aplicar regras do dominio.
     */
    @Transactional(readOnly = true)
    public Map<Long, Aluno> carregarAlunos(Collection<Long> ids) {
        Map<Long, Aluno> alunos = new LinkedHashMap<>();
        alunoRepository.findAllById(ids)
                .forEach(entidade -> alunos.put(entidade.getId(), entidade.paraDominio()));
        return alunos;
    }

    /**
     * Persiste o total de cursos ganhos calculado pelo dominio.
     */
    @Transactional
    public AlunoResumo atualizarCursosGanhos(Long alunoId, int cursosGanhos) {
        AlunoEntity entidade = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: id " + alunoId));
        entidade.setCursosGanhos(cursosGanhos);
        return AlunoResumo.de(alunoRepository.save(entidade));
    }

    private AlunoEntity entidadePorNome(String nome) {
        return alunoRepository.findByNome(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: " + nome));
    }
}
