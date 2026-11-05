# ADR-004 — Premiação síncrona; notificação e analytics por eventos (Outbox + RabbitMQ)

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-11-04 |
| **Desafio** | B1 · Desafio 4 — Comunicação e eventos |
| **Relaciona** | ADR-002 (portas da premiação), ADR-003 (chamada síncrona à IA) |

## Contexto

O enunciado descreve a conclusão de um curso. No nosso Core, o fluxo equivalente é a
**conquista**: ao encerrar o mês, o sistema registra a premiação, calcula e credita o curso
(RF02) e precisa **notificar o aluno** (RF05) e **alimentar o analytics**. Uma falha de notificação
não pode impedir a premiação.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **Tolerância a falhas** | Notificação ou broker fora do ar não podem afetar a premiação. A notificação sai do caminho da requisição, o que altera a **comunicação** (síncrona → assíncrona). |
| **Recuperabilidade** | Evento de premiação não pode ser **perdido**, só atrasado. Exige armazenamento durável **antes** e **depois** do broker, o que altera os **dados** (tabela outbox) e a **operação** (relay + filas duráveis). |
| **Consistência** | Não pode existir premiação sem evento nem evento sem premiação. Exige que o evento seja gravado **na mesma transação**, o que altera os **dados** e a **estrutura** (padrão Transactional Outbox). |

Desacoplamento é consequência (produtor não conhece consumidores), não driver. Latência e
disponibilidade da premiação ficam protegidas pelos três ASRs acima.

## RPCs

- Operações essenciais do Core devem concluir mesmo com consumidor externo indisponível;
- adoção de broker exige custo operacional (mais um container, monitoramento, topologia).

## Perguntas do Tech Lead

- **Qual resposta precisa ser imediata?** O resultado da premiação (quem ganhou, novo total de
  cursos). O aluno e o front precisam dele na hora e com consistência forte, então continua
  **REST síncrono**, numa transação.
- **Qual pode ser eventual?** Notificação ao aluno e analytics. Segundos de atraso não mudam nada
  para o negócio.
- **Que ASR justificaria mensageria?** **Recuperabilidade** com **mais de um consumidor
  independente**. Com um único consumidor, o outbox mais uma chamada com retry bastariam. Com
  notificação **e** analytics consumindo o mesmo fato em ritmos diferentes, filas duráveis com
  fan-out são o problema que um broker resolve.
- **RabbitMQ/Kafka resolvem um problema real?** O RabbitMQ resolve: filas duráveis por
  consumidor, roteamento (topic) e ack por mensagem, com operação simples. O Kafka resolveria
  replay e streaming de alto volume, que **não são problema do caso** (poucos eventos por mês, sem
  necessidade de reprocessar histórico), e traria um custo operacional que não se paga para uma
  equipe de 6.

## Alternativas

| | Tolerância a falhas | Recuperabilidade | Consistência | Custo |
|---|---|---|---|---|
| **A — REST síncrono** para notificação dentro do encerrar-mês | ❌ consumidor fora: premiação falha ou paga timeout (o D3 mediu ~1,5 s) | ❌ notificação perdida | — | baixo |
| **B — Evento em memória** (`@Async`/`ApplicationEvent`) | ✅ | ❌ processo cai = evento perdido | ❌ | baixo |
| **C — Publicar direto no RabbitMQ após o commit** | ✅ consumidor fora | ⚠️ broker fora no instante = evento perdido | ❌ commit e publish separados | médio |
| **D — Outbox + relay + RabbitMQ** ✅ | ✅ | ✅ outbox antes, fila durável depois | ✅ mesma transação | médio |
| **E — Outbox + Kafka** | ✅ | ✅ (+ replay) | ✅ | alto, sem problema que justifique |

## Decisão

```
POST /encerrar-mes ──► [transação] historico + credito + evento_outbox ──► HTTP 200  (síncrono)
                                                      │
                          relay (a cada 2 s, publisher confirms)
                                                      ▼
                     RabbitMQ  exchange gamificacao.eventos (topic)
                        premiacao.concedida ─► fila notificacao.premiacao ─► serviço notificacao (Python)
                        premiacao.concedida ─► fila analytics.premiacao   ─► (consumidor futuro)
```

- porta de saída `EventosDePremiacao` no núcleo da premiação. O adapter grava no outbox via
  `RegistroDeEventos`, com `Propagation.MANDATORY` (falha se chamado fora de transação);
- módulo `eventos`: outbox, relay ordenado (para na primeira falha) e publicador RabbitMQ com
  **publisher confirms** (só marca como publicado depois do ack do broker);
- filas **duráveis**, declaradas pelo próprio Core, para que nenhuma mensagem se perca por falta de
  fila;
- entrega **pelo menos uma vez**: o consumidor é **idempotente** (descarta `messageId` repetido) e
  faz ack manual depois de processar;
- o Core **não depende** do broker para subir (sem `depends_on`).

## Consequências

**Positivas**
- premiação responde em ~15–30 ms com consumidor **ou** broker fora do ar;
- consumidor fora: a mensagem espera na fila e é entregue quando ele volta;
- broker fora: os eventos esperam no outbox e são entregues **em ordem** quando ele volta (no lab:
  4 eventos após ~3 min, zero perda);
- analytics pode nascer depois e consumir sem mudar o produtor.

**Negativas / trade-offs**
- **consistência eventual**: a notificação chega segundos depois (intervalo do relay + fila);
- **duplicatas possíveis** (pelo menos uma vez), então todo consumidor precisa ser idempotente;
- mais operação: broker, tabela outbox (que precisará de limpeza dos publicados), relay agendado;
- o relay segura uma transação do banco enquanto espera a confirmação do broker (até 5 s). É
  aceitável no volume atual e deve ser revisto se o volume crescer.

## Evidências

[`docs/arquitetura/evidencias/d4/`](../arquitetura/evidencias/d4/)
