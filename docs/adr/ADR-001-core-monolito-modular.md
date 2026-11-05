# ADR-001 — Core organizado como Monólito Modular por capacidade

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-11-04 |
| **Desafio** | B1 · Desafio 1 — Drivers e evolução do Core |
| **Substitui** | Estrutura em camadas técnicas da AC1 (`controller/ service/ repository/ entity/ dto/ domain/`) |

## Contexto

O MVP da AC1 (premiação por engajamento no fórum) foi construído em camadas técnicas. Funcionava,
mas a análise com `jdeps` mostrou o custo dessa organização mesmo num sistema pequeno:

- uma única classe, o `ForumService`, dependia de **12 classes em 4 pacotes** e concentrava
  quatro responsabilidades diferentes (cadastro de aluno, registro de participação, ranking e premiação);
- o contrato da API (`dto`) dependia direto das entidades JPA (`dto -> entity`), ou seja, uma
  mudança de tabela vazava para o JSON;
- a participação no fórum tinha `@ManyToOne` para a entidade do aluno: a persistência do ranking
  estava amarrada à persistência do cadastro.

O cenário do B1 leva a plataforma de ~1.000 para até 100.000 usuários. O ranking (RF01) cresce
mais que o resto, enquanto conquistas, cursos e recompensas (RF02) continuam mudando.

## ASRs que dirigem a decisão

| ASR | Como altera a arquitetura |
|---|---|
| **Manutenibilidade** | Capacidades que mudam em ritmos diferentes precisam de fronteiras próprias para que mudar uma não exija mexer nas outras — altera a **estrutura** do código. |
| **Escalabilidade** | O ranking pode precisar de escala própria no futuro — exige que ele já esteja isolado, com dependências explícitas, para poder ser extraído — altera a **estrutura** e prepara a **implantação**. |
| **Complexidade operacional** | Cada serviço novo traz deploy, rede, contrato, observabilidade e falha parcial — limita o número de unidades de **implantação/operação**. |

## RPCs

- Core em Java 17 / Spring Boot 4 (legado da AC1, deve ser preservado);
- equipe de 6 desenvolvedores (premissa do caso);
- banco relacional (H2 em dev, PostgreSQL em homologação) — sem troca de tecnologia de dados;
- legado da AC1: regras de domínio construídas por TDD e cenários BDD devem continuar passando sem alteração de lógica.

## Alternativas

| | Prós | Contras |
|---|---|---|
| **A — Manter camadas técnicas** | Zero esforço; todos conhecem o padrão. | O acoplamento medido (fan-out 12 do `ForumService`, `dto -> entity`) cresce a cada capacidade nova; extrair o ranking exigiria desmontar todas as camadas. |
| **B — Monólito Modular** ✅ | Fronteiras por capacidade; dependências explícitas e verificáveis; continua **1 deploy, 1 banco**; ranking fica pronto para extração. | Mais classes e contratos internos; regras de fronteira precisam ser vigiadas (senão o monólito "apodrece" de volta). |
| **C — Extrair o ranking como serviço agora** | Escala e deploy independentes do ranking. | Não há medição de gargalo que justifique; adiciona rede, consistência entre bancos, pipeline e monitoramento extra para uma equipe de 6. |

## Decisão

Reorganizar o Core em **módulos por capacidade de negócio**, mantendo um único executável Spring Boot:

| Módulo | Capacidade | RF |
|---|---|---|
| `aluno` | cadastro e consulta de alunos | base |
| `engajamento` | participações no fórum e ranking do mês | RF01 |
| `premiacao` | encerramento do mês e concessão do curso | RF02 |
| `compartilhado` | erros, validação de mês, configuração | — |

Dentro de cada módulo: `web` (entrada HTTP) → `aplicacao` (**contrato público do módulo**) →
`dominio` (regras, sem framework) e `persistencia` (JPA, **privada do módulo**).

Regras (verificadas pelo `ArquiteturaModularTest`, que quebra o build):

1. um módulo só usa de outro o pacote `aplicacao` (e as classes de `dominio` herdadas da AC1);
2. `persistencia` e `web` são privadas do módulo;
3. dependência em sentido único: `premiacao → engajamento → aluno`, sem ciclos;
4. `dominio` não depende de Spring, JPA nem de outras camadas.

Entre módulos, a referência a outra capacidade é feita por **id** (a participação guarda
`aluno_id`, sem `@ManyToOne`).

## Consequências

**Positivas**
- nenhuma aresta de dependência entre módulos atinge `persistencia` ou `web` (antes: o service
  central alcançava as entidades e repositórios de todas as capacidades);
- a maior classe passou de 12 para 8 dependências, e cada service tem uma responsabilidade;
- o contrato HTTP ficou idêntico (rotas, schemas e respostas comparados antes/depois);
- o ranking (`engajamento`) não depende de `premiacao` e acessa alunos só pelo `AlunoService` —
  se for extraído, só esse ponto vira cliente HTTP.

**Negativas / trade-offs aceitos**
- o total de arestas de dependência subiu de 25 para 34: há mais classes e contratos explícitos;
- sem `@ManyToOne`, a integridade aluno↔participação passa a ser garantida pela aplicação
  (o `EngajamentoService` só grava após `AlunoService.buscarPorNome`), não por FK gerada pelo JPA;
- ainda é um processo e um banco: o ranking não escala sozinho;
- os domínios da AC1 continuam se referenciando (`ParticipacaoForum` usa `Aluno`) — aceito para
  não reescrever o código do TDD; o Desafio 2 trata da proteção do domínio.

## Gatilho de revisão

Reavaliar a extração do módulo `engajamento` para um serviço quando houver **medição** (Desafio 6)
de pelo menos um destes sinais:

- latência p95 de `GET /api/forum/engajamento` acima do objetivo sob carga real;
- necessidade de escalar o ranking sem escalar o resto (CPU/memória do Core dominadas por ele);
- frequência de deploy do ranking muito diferente das demais capacidades.

## Evidências

[`docs/arquitetura/evidencias/d1/`](../arquitetura/evidencias/d1/)
