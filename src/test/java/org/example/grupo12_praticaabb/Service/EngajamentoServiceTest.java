package org.example.grupo12_praticaabb.Service;

import org.example.grupo12_praticaabb.DTO.EngajamentoResponse;
import org.example.grupo12_praticaabb.Entity.UsuarioEntity;
import org.example.grupo12_praticaabb.Exception.RecursoNaoEncontradoException;
import org.example.grupo12_praticaabb.Repositorio.ComentarioAjudaRepository;
import org.example.grupo12_praticaabb.Repositorio.TopicoRepository;
import org.example.grupo12_praticaabb.Repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EngajamentoServiceTest {
    @Test
    void deveCalcularEngajamentoComBaseNasDuasQuantidades() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        TopicoRepository topicos = mock(TopicoRepository.class);
        ComentarioAjudaRepository comentarios = mock(ComentarioAjudaRepository.class);
        UsuarioEntity aluno = mock(UsuarioEntity.class);

        when(aluno.getId()).thenReturn(10L);
        when(aluno.getNome()).thenReturn("Pedro");
        when(usuarios.findById(10L)).thenReturn(Optional.of(aluno));
        when(topicos.countByUsuarioId(10L)).thenReturn(4L);
        when(comentarios.countByUsuarioId(10L)).thenReturn(6L);

        EngajamentoService service = new EngajamentoService(usuarios, topicos, comentarios);
        EngajamentoResponse resposta = service.calcularEngajamento(10L);

        assertEquals(10, resposta.engajamento());
        assertEquals(4, resposta.topicos());
        assertEquals(6, resposta.comentariosAjuda());
        verify(topicos).countByUsuarioId(10L);
        verify(comentarios).countByUsuarioId(10L);
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExiste() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.findById(99L)).thenReturn(Optional.empty());
        EngajamentoService service = new EngajamentoService(usuarios, mock(TopicoRepository.class), mock(ComentarioAjudaRepository.class));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.calcularEngajamento(99L));
    }
}
