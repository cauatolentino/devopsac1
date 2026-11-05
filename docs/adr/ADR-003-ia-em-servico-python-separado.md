# ADR-003 — IA em serviço Python separado, com degradação controlada pelo Core

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-11-04 |
| **Desafio** | B1 · Desafio 3 — Serviço Python e IA |
| **Relaciona** | ADR-001 (Core modular), ADR-002 (portas e adapters) |

## Contexto

A plataforma ganha **recomendação inteligente (RF03)** e **assistente educacional com RAG/LLM
(RF04)**. Os modelos são desenvolvidos em Python, têm ciclo de atualização próprio e carga
variável, e **podem ficar indisponíveis**. Cadastro, participação, ranking e premiação são
operações essenciais e não podem parar por causa da IA.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **Tolerância a falhas** | A IA cair não pode derrubar nem travar o Core, o que exige **processo separado**, timeout em toda chamada e fallback decidido no Core. Altera **comunicação** e **estrutura**. |
| **Deploy independente** | Modelo novo não pode exigir rebuild/redeploy do Core, o que exige **imagem e container próprios**. Altera a **implantação**. |
| **Escalabilidade** | A carga da IA varia (picos de uso do assistente, inferência mais pesada) de forma diferente do Core, o que exige **escalar a IA sozinha**. Altera a **implantação/operação**. |

Não são ASR aqui: **interoperabilidade** (REST/JSON resolve sem esforço, é consequência e não
driver), **desempenho** (vira uma restrição de orçamento de latência, o timeout de 1,5 s, e não
muda a estrutura) e **manutenibilidade** (já tratada nos ADR-001/002).

## RPCs

- Core permanece Java/Spring Boot;
- ML/RAG utilizam Python;
- indisponibilidade da IA não pode bloquear o fluxo essencial.

## Perguntas do Tech Lead

- **Python por si só justifica um serviço?** **Não.** Daria para chamar Python em processo
  (GraalPy, por exemplo) ou por script. O que justifica a separação são os três ASRs: falha
  isolada, ciclo de deploy próprio e escala própria.
- **ML e RAG precisam da mesma fronteira?** **Não.** A recomendação é inferência leve e
  previsível. O RAG depende de provedor LLM, base vetorial e documentos, com latência de segundos,
  custo por token e modos de falha próprios (cota, alucinação, provedor fora). Juntar os dois
  faria a falha ou o pico de um afetar o outro. Decisão: **dois serviços**, `ia-recomendacao`
  (implementado agora) e `ia-assistente` (contrato e lugar definidos, implementação na PoC da AF).
- **Qual comportamento degradado é aceitável?**
  - recomendação: **catálogo padrão** (sugestões genéricas) com `degradado: true` e HTTP 200;
  - assistente (futuro): mensagem "assistente indisponível no momento", sem afetar o resto da tela;
  - fluxo essencial: **nenhuma degradação**, porque não depende da IA (regra ArchUnit
    `fluxoEssencialNaoDependeDaIa`).

## Alternativas

| | Prós | Contras |
|---|---|---|
| **A — IA embutida no Core** (Java ou Python em processo) | Sem rede; um deploy. | Falha/lentidão do modelo atinge o Core; modelo novo exige redeploy do Core; não escala separado; viola a RPC "IA em Python" na prática. |
| **B — Módulo acoplado** (lib/módulo do Core que chama scripts Python) | Fronteira de código explícita. | Mesmo processo e mesmo deploy: não resolve nenhum dos 3 ASRs. |
| **C — Serviço Python independente** ✅ | Falha isolada; deploy e escala próprios; time de dados trabalha no seu stack. | Rede (latência, timeout), contrato a versionar, mais um container para operar. |

## Decisão

1. **Serviço `ia-recomendacao`** em Python/FastAPI, em container próprio, com contrato REST/JSON
   (`POST /recomendacoes`, `GET /saude`). Os testes (pytest) rodam no `docker build`, e a imagem
   só é gerada se passarem.
2. No Core, novo módulo **`recomendacao`** (RF03), seguindo o padrão do ADR-002:
   - porta de saída `MotorDeRecomendacao`;
   - adapter `MotorDeRecomendacaoHttp` (RestClient), com **timeout de conexão de 500 ms e de
     leitura de 1500 ms** e **toda falha traduzida** para `MotorDeRecomendacaoIndisponivel`;
   - o **caso de uso decide o fallback** (`CatalogoPadrao`). A política de degradação é regra do
     Core, não do adapter.
3. **Serviço `ia-assistente` (RAG) separado**, planejado para a PoC da AF.
4. O Core **não declara `depends_on`** da IA no docker-compose, porque precisa subir sem ela.

## Consequências

**Positivas**
- IA fora do ar: recomendação responde em ~15 ms com o catálogo padrão; ranking e premiação
  continuam normais;
- IA lenta: o Core desiste em 1,5 s e responde degradado, sem thread presa indefinidamente;
- IA de volta: recuperação automática, sem restart do Core;
- modelo pode ser trocado (o contrato traz `versaoModelo`) sem tocar no Java.

**Negativas / trade-offs**
- com a IA **lenta**, cada requisição de recomendação paga até 1,5 s antes do fallback. Um
  *circuit breaker* evitaria isso, mas foi **adiado** até que as métricas (Desafio 6) mostrem
  volume que justifique;
- o contrato JSON passa a ser versionado entre dois times/linguagens;
- mais um container para build, deploy e monitoramento (Desafio 6).

## Emenda — 2026-11-04 (Desafio 6): a métrica derrubou uma premissa

O gatilho previsto acima disparou. Em HOMOL, o painel "Latência da chamada à IA" mostrou falhas
de **8 s** com a IA parada, embora a resposta continuasse HTTP 200 degradada. A causa: o Core
chama a IA **pelo nome do container**, e com o container parado o nome some do DNS do Docker. A
**resolução de DNS não é coberta** pelos timeouts de conexão e leitura. No mini-lab do D3 isso não
apareceu porque a IA era acessada por `localhost`.

Correção, sem mexer no caso de uso (decorators da porta, no adapter):

1. **Disjuntor** (`DisjuntorDoMotor`): 2 falhas seguidas abrem por 30 s. Enquanto aberto, falha na
   hora sem rede nem DNS; depois, uma chamada de prova (sucesso fecha, falha reabre). Métrica
   `gamificacao_recomendacao_disjuntor_aberto`.
2. **Prazo total** (`PrazoTotalDoMotor`): 2 s por chamada, cobrindo DNS, conexão e leitura.

Resultado medido (10 chamadas com a IA parada): antes, a 1ª chamada levava **8,2 s**; depois,
**≤ 2,1 s** nas 2 primeiras e **~15 ms** nas demais. A recuperação continua automática.

Novo trade-off aceito: quando a IA volta, ela só é usada de novo depois de **até 30 s** (o tempo
do disjuntor aberto). Detalhes em [`evidencias/d6/04-...`](../arquitetura/evidencias/d6/04-metrica-derrubou-premissa-adr003.txt).

## Evidências

[`docs/arquitetura/evidencias/d3/`](../arquitetura/evidencias/d3/) · emenda: [`docs/arquitetura/evidencias/d6/`](../arquitetura/evidencias/d6/)
