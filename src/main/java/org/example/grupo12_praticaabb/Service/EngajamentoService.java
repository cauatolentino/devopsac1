package org.example.grupo12_praticaabb.Service;

import org.example.grupo12_praticaabb.Domain.Usuario;
import org.example.grupo12_praticaabb.DTO.EngajamentoResponse;
import org.example.grupo12_praticaabb.DTO.UsuarioResponse;
import org.example.grupo12_praticaabb.Entity.ComentarioAjudaEntity;
import org.example.grupo12_praticaabb.Entity.TopicoEntity;
import org.example.grupo12_praticaabb.Entity.UsuarioEntity;
import org.example.grupo12_praticaabb.Exception.RecursoNaoEncontradoException;
import org.example.grupo12_praticaabb.Repositorio.ComentarioAjudaRepository;
import org.example.grupo12_praticaabb.Repositorio.TopicoRepository;
import org.example.grupo12_praticaabb.Repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EngajamentoService {
    private final UsuarioRepository usuarioRepository;
    private final TopicoRepository topicoRepository;
    private final ComentarioAjudaRepository comentarioRepository;

    public EngajamentoService(UsuarioRepository usuarioRepository, TopicoRepository topicoRepository,
                              ComentarioAjudaRepository comentarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.topicoRepository = topicoRepository;
        this.comentarioRepository = comentarioRepository;
    }

    public UsuarioResponse criarUsuario(String nome, String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        }
        UsuarioEntity usuario = usuarioRepository.save(new UsuarioEntity(nome, email));
        return resposta(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(this::resposta).toList();
    }

    public void adicionarTopico(Long usuarioId, String titulo) {
        UsuarioEntity usuario = buscar(usuarioId);
        topicoRepository.save(new TopicoEntity(titulo, usuario));
    }

    public void adicionarComentarioAjuda(Long usuarioId, String conteudo) {
        UsuarioEntity usuario = buscar(usuarioId);
        comentarioRepository.save(new ComentarioAjudaEntity(conteudo, usuario));
    }

    @Transactional(readOnly = true)
    public EngajamentoResponse calcularEngajamento(Long usuarioId) {
        UsuarioEntity usuario = buscar(usuarioId);
        int topicos = Math.toIntExact(topicoRepository.countByUsuarioId(usuarioId));
        int comentarios = Math.toIntExact(comentarioRepository.countByUsuarioId(usuarioId));
        Usuario dominio = new Usuario(usuario.getId(), usuario.getNome(), topicos, comentarios);
        return new EngajamentoResponse(dominio.getId(), dominio.getNome(),
                dominio.getQuantidadeTopicos(), dominio.getQuantidadeComentariosAjuda(), dominio.calcularEngajamento());
    }

    private UsuarioResponse resposta(UsuarioEntity usuario) {
        int topicos = Math.toIntExact(topicoRepository.countByUsuarioId(usuario.getId()));
        int comentarios = Math.toIntExact(comentarioRepository.countByUsuarioId(usuario.getId()));
        Usuario dominio = new Usuario(usuario.getId(), usuario.getNome(), topicos, comentarios);
        return new UsuarioResponse(dominio.getId(), dominio.getNome(), usuario.getEmail(), topicos, comentarios, dominio.calcularEngajamento());
    }

    private UsuarioEntity buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }
}
