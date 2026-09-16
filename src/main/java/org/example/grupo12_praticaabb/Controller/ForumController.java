package org.example.grupo12_praticaabb.Controller;

import org.example.grupo12_praticaabb.DTO.ParticipacaoRequestDTO;
import org.example.grupo12_praticaabb.Domain.Aluno;
import org.example.grupo12_praticaabb.Service.ForumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forums")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    // NOVO ENDPOINT: Recebe os dados usando o seu DTO (Isso fará os avisos sumirem!)
    @PostMapping("/participacao")
    public ResponseEntity<String> registrarParticipacao(@RequestBody ParticipacaoRequestDTO dto) {
        // Ao chamar os métodos do DTO aqui, o IntelliJ reconhece que eles estão sendo usados
        System.out.println("Recebendo participação do Aluno ID: " + dto.getIdAluno());
        System.out.println("Tópicos: " + dto.getQuantidadeTopicos());

        // No futuro, você passará esse DTO para o Service salvar no banco
        return ResponseEntity.ok("Participação recebida com sucesso!");
    }

    // Endpoint antigo: Premia o aluno
    @PostMapping("/{id}/premiar")
    public ResponseEntity<Aluno> premiarMelhorAluno(@PathVariable Long id) {
        try {
            Aluno vencedor = forumService.realizarPremiacaoDoForum(id);
            return ResponseEntity.ok(vencedor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}