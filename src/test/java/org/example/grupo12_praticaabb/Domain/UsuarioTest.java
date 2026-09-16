package org.example.grupo12_praticaabb.Domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {
    @Test
    void deveSomarTopicosEComentariosParaCalcularEngajamento() {
        Usuario usuario = new Usuario(1L, "Ana", 7, 3);
        assertEquals(7, usuario.getQuantidadeTopicos());
        assertEquals(3, usuario.getQuantidadeComentariosAjuda());
        assertEquals(10, usuario.calcularEngajamento());
        assertEquals(1L, usuario.getId());
        assertEquals("Ana", usuario.getNome());
    }

    @Test
    void deveRetornarZeroQuandoNaoHaAtividades() {
        assertEquals(0, new Usuario(2L, "Bruno", 0, 0).calcularEngajamento());
    }

    @Test
    void deveAceitarApenasValoresNaoNegativos() {
        assertDoesNotThrow(() -> new Usuario(3L, "Carla", 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Usuario(4L, "Diego", -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Usuario(5L, "Eva", 0, -1));
    }
}
