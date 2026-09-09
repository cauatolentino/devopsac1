# Evidências do ciclo TDD — US1

Ciclo completo **RED → GREEN → BLUE** sobre o pacote `com.example.grupo12_praticaatdd.domain`.

| Etapa | Comando | Resultado | Arquivo |
|---|---|---|---|
| RED | `./mvnw test` | 16 testes, **11 falhas + 2 erros**, BUILD FAILURE | [`01-red-testes-falhando.txt`](01-red-testes-falhando.txt) |
| GREEN | `./mvnw verify` | **16/16 passando**, BUILD SUCCESS, cobertura com amarelo e vermelho | [`02-green-testes-passando.txt`](02-green-testes-passando.txt) · [`02-green-cobertura.md`](02-green-cobertura.md) |
| BLUE | `./mvnw clean verify` | **16/16 passando**, **100% sem amarelo e sem vermelho** | [`03-blue-testes-passando.txt`](03-blue-testes-passando.txt) · [`03-blue-cobertura.md`](03-blue-cobertura.md) |

Relatório HTML do JaCoCo da etapa BLUE: [`jacoco-blue/index.html`](jacoco-blue/index.html)
(abrir no navegador; é a cópia de `target/site/jacoco` gerada pelo build).

## Estrutura de pacotes pedida no enunciado

```
src/main/java/com/example/grupo12_praticaatdd/domain/       <- pacote Domain
    Aluno.java
    ParticipacaoForum.java
    Forum.java

src/test/java/com/example/grupo12_praticaatdd/domaintest/   <- pacote DomainTest
    AlunoTest.java
    ParticipacaoForumTest.java
    ForumTest.java
```

## Rastreabilidade BDD → teste → autor

Cada cenário escrito na planilha virou pelo menos um teste automatizado:

| # | Cenário BDD | Autor | Teste |
|---|---|---|---|
| 1 | DADO um fórum com a participação de vários alunos no mês / E com engajamentos diferentes / QUANDO o mês é encerrado / ENTÃO o aluno com maior engajamento deve ganhar 1 curso | **Cauã Tolentino** | `ForumTest.devePremiarAlunoComMaiorEngajamento` |
| 2 | DADO alunos com diferentes quantidades de tópicos e comentários de ajuda / QUANDO o engajamento de um aluno é calculado / ENTÃO deve ser a soma dos tópicos com os comentários de ajuda | **Pedro Pizzi** | `ParticipacaoForumTest.engajamentoDeveSomarTopicosEComentarios` |
| 3 | DADO um fórum com vários alunos participando / QUANDO o mês é encerrado / ENTÃO somente o aluno de maior engajamento é premiado | **Henry Tanaka** | `ForumTest.somenteAlunoDeMaiorEngajamentoEPremiado` |
| 4 | DADO um fórum sem nenhuma participação no mês / QUANDO o mês é encerrado / ENTÃO nenhum aluno deve ser premiado | **Cauã Tolentino** | `ForumTest.naoDevePremiarNinguemQuandoNaoHouveParticipacao` |

Os demais testes (`naoDeveCriarAlunoComNomeNulo`, `naoDeveCriarParticipacaoComTopicosNegativos`
etc.) são testes de guarda que o grupo acrescentou para fechar os casos de borda do domínio.

## Como reproduzir

```bash
# RED: voltar o domínio ao commit dos stubs
git checkout 8cf3d18 -- src/main/java/com/example/grupo12_praticaatdd/domain
./mvnw test          # -> BUILD FAILURE

# GREEN / BLUE: estado atual
git checkout HEAD -- src/main/java/com/example/grupo12_praticaatdd/domain
./mvnw clean verify  # -> BUILD SUCCESS + jacoco:check em 100%
```
