package org.example.grupo12_praticaabb.Service;

import org.example.grupo12_praticaabb.Domain.Aluno;
import org.example.grupo12_praticaabb.Domain.Forum;
import org.example.grupo12_praticaabb.Domain.ParticipacaoForum;
import org.example.grupo12_praticaabb.Entity.ForumEntity;
import org.example.grupo12_praticaabb.Entity.ParticipacaoForumEntity;
import org.example.grupo12_praticaabb.Repository.ForumRepository;
import org.springframework.stereotype.Service;

@Service
public class ForumService {

    private final ForumRepository forumRepository;

    public ForumService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    public Aluno realizarPremiacaoDoForum(Long idForum) {
        ForumEntity forumEntity = forumRepository.findById(idForum)
                .orElseThrow(() -> new RuntimeException("Fórum não encontrado"));

        Forum forumDominio = new Forum();

        for (ParticipacaoForumEntity part : forumEntity.getParticipacoes()) {
            Aluno aluno = new Aluno(part.getNomeAluno());
            ParticipacaoForum participacao = new ParticipacaoForum(
                    aluno,
                    part.getQuantidadeTopicos(),
                    part.getQuantidadeComentarios()
            );
            forumDominio.registrar(participacao);
        }

        return forumDominio.premiarAluno();
    }
}