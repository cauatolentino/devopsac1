# ADR-002 — Ports & Adapters apenas no módulo de premiação

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-11-04 |
| **Desafio** | B1 · Desafio 2 — Organização interna do Core |
| **Relaciona** | ADR-001 (Monólito Modular) |

## Contexto

As regras de ranking, moedas, badges e recompensas mudam com frequência. No fim do Desafio 1 o
domínio **já era independente** de framework: o ArchUnit garante que `*.dominio` não usa Spring,
JPA nem outras camadas. O acoplamento que sobrava estava um nível acima, no caso de uso da
premiação:

- o `PremiacaoService` dependia das **implementações** `EngajamentoService` e `AlunoService`.
  Para testá-lo era preciso subir Spring e banco, e trocar a origem do ranking (por exemplo, um
  serviço remoto) exigiria mudar o caso de uso;
- encontramos um **defeito** de regra de negócio: encerrar o mesmo mês 3 vezes concedia 3 cursos.
  A regra "um mês é premiado uma única vez" exige que a premiação tenha memória própria, ou seja,
  uma nova dependência de persistência entrando justamente no módulo cujas regras mais mudam.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **Modificabilidade** | As regras de conquista mudam com frequência. O núcleo da premiação precisa mudar sem tocar em banco, HTTP ou outros módulos, o que altera a **estrutura** (núcleo cercado por portas). |
| **Testabilidade** | A regra tem que ser verificável sem infraestrutura. As portas permitem ligar adapters falsos, o que altera a **estrutura** e o modo de teste. |
| **Facilidade de evolução** | Se o ranking virar serviço (gatilho do ADR-001), só o adapter troca, o que prepara a **comunicação** (em processo hoje, HTTP amanhã) sem mexer no núcleo. |

## RPCs

- Core continua Spring Boot;
- regras existentes (domínio do TDD da AC1) devem ser reaproveitadas sem alteração;
- proibido reescrever o produto inteiro só para aplicar um padrão.

## Qual dependência precisava ser invertida?

Só uma: **caso de uso da premiação → coisas externas a ele** (o módulo de engajamento, o módulo de
alunos e a persistência da premiação). O domínio já estava protegido, e `aluno` e `engajamento`
são fluxos simples de cadastro/consulta, onde portas seriam custo sem retorno.

## Alternativas

| | Prós | Contras |
|---|---|---|
| **A — Manter camadas dentro do módulo** (aplicação chama serviços e JPA concretos) | Nenhum custo novo. | Caso de uso só testável com Spring + banco; trocar a fonte do ranking exige mexer na regra; a nova persistência entraria direto no núcleo. |
| **B — Ports & Adapters só no módulo `premiacao`** ✅ | Inverte exatamente a dependência problemática; núcleo testável com fakes; troca de adapter sem mudar o núcleo. | Mais tipos (4 portas, 4 adapters); dois estilos convivendo no Core. |
| **C — Clean/Hexagonal em todos os módulos** (anéis, módulos Maven separados) | Uniformidade total. | Custo desproporcional: `aluno` e `engajamento` ganhariam portas para CRUD; viola a RPC de não reescrever o produto. |

## DDD, Hexagonal, Clean e Onion: concorrentes ou combináveis?

**Combináveis**, cada um em um nível diferente:

- **DDD (estratégico)** decide **quais** fronteiras existem. Os módulos `aluno`, `engajamento` e
  `premiacao` funcionam como bounded contexts, com linguagem ubíqua do fórum (participação,
  engajamento, premiação).
- **Hexagonal** decide **como** um contexto fala com o mundo: portas de entrada e de saída mais
  adapters.
- **Clean / Onion** fornecem a **regra de dependência** (tudo aponta para o domínio). Adotamos a
  regra, verificada por ArchUnit, mas não a cerimônia completa de anéis.

## Decisão

Aplicar Ports & Adapters **somente** no módulo `premiacao`:

```
premiacao/
├── dominio/                       Forum (regra da US1, intocada)
├── aplicacao/
│   ├── PremiacaoService           caso de uso, só conhece portas
│   ├── porta/entrada/EncerrarMes
│   └── porta/saida/               FonteDeEngajamento, CreditoDeCursos, HistoricoDePremiacoes
└── adaptador/
    ├── entrada/web/               PremiacaoController (depende da porta EncerrarMes)
    ├── saida/modulos/             EngajamentoDoForumAdapter, CursosDoAlunoAdapter
    └── saida/persistencia/        HistoricoDePremiacoesJpa + tabela premiacao_mensal (UNIQUE no mês)
```

Concessão pragmática: `PremiacaoService` mantém `@Service` e `@Transactional`. Sem contexto Spring
essas anotações não têm efeito (o teste unitário prova isso), e removê-las exigiria uma classe de
configuração só para montar o objeto.

## Consequências

**Positivas**
- o caso de uso roda em teste unitário puro, com adapters falsos: 5 testes em ~0,01 s, sem Spring
  e sem banco;
- o mesmo contrato de teste passa com o adapter em memória e com o JPA, ou seja, os dois são
  intercambiáveis;
- o defeito do prêmio repetido foi corrigido. A constraint UNIQUE protege inclusive contra duas
  requisições simultâneas;
- o único ponto que conhece outros módulos é `adaptador.saida.modulos`, verificado por ArchUnit;
- os testes de domínio da AC1 continuaram idênticos.

**Negativas / trade-offs**
- mais indireção para ler o fluxo (controller → porta → caso de uso → porta → adapter);
- dois estilos internos no Core (camadas em `aluno`/`engajamento`, hexagonal em `premiacao`);
  as regras do ArchUnit documentam cada um;
- nova tabela `premiacao_mensal`, criada por `ddl-auto=update` (em produção real, seria uma
  migração versionada).

## Evidências

[`docs/arquitetura/evidencias/d2/`](../arquitetura/evidencias/d2/)
