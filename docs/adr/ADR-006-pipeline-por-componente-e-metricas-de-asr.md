# ADR-006 — Um Jenkinsfile com ciclos por componente; métricas que validam ASRs

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-11-04 |
| **Desafio** | B1 · Desafio 6 — CI/CD e observabilidade |
| **Relaciona** | ADR-001 a ADR-005 (cada um ganha uma métrica ou verificação em HOMOL) |

## Contexto

Depois dos Desafios 1–5, a solução tem: Core Java (modular), `ia-recomendacao` (Python),
`notificacao` (Python), simulador IoT (Python), RabbitMQ (AMQP + MQTT), PostgreSQL e front Vue.
A arquitetura só é sustentável se cada parte puder ser **testada, implantada e observada** com
segurança, e se as decisões anteriores puderem ser **verificadas** depois de implantadas, não só
no papel.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **Deploy independente** | O ADR-003 prometeu que a IA evolui sem redeploy do Core. A esteira precisa entregar isso: build, teste e deploy **por componente**. Altera a **implantação** (pipeline). |
| **Observabilidade** | Os ASRs anteriores (degradação da IA, outbox, IoT, gatilho do ranking) só são verificáveis com métricas próprias. Altera a **operação** (Actuator, Micrometer, Prometheus, Grafana, alertas). |
| **Confiabilidade** | Nada vai para HOMOL sem passar pelos portões, e o ambiente implantado precisa provar os ADRs. Altera a **implantação** (o que bloqueia deploy). |

Testabilidade, recuperabilidade e desempenho entram **através** desses três: testabilidade
como portão, recuperabilidade e desempenho como métricas e alertas.

## RPCs

- Jenkins é a base da esteira (Jenkinsfile da AC1 evoluído, não substituído);
- Prometheus e Grafana são as ferramentas do laboratório;
- artefatos e configurações versionados: `Jenkinsfile`, `docker/prometheus/*.yml`,
  `docker/grafana/**` e `docker-compose*.yml` estão no Git.

## Perguntas do Tech Lead

- **Serviços independentes exigem pipelines independentes?** Exigem **ciclos independentes**,
  não necessariamente arquivos separados. Num monorepo com uma equipe de 6, um Jenkinsfile com
  CI **paralelo por componente** e deploy **por componente** (`COMPONENTE=ia-recomendacao`,
  `docker compose up --no-deps`) entrega a independência com um lugar só para manter. Gatilho
  para separar em pipelines/repos: times diferentes donos de cada serviço ou cadência de release
  muito diferente.
- **O que deve bloquear deploy?**
  1. testes JUnit e cenários BDD do Core;
  2. **regras de arquitetura (ArchUnit)**, para que um acoplamento proibido quebre o build;
  3. JaCoCo 100%;
  4. pytest **dentro do build** de cada imagem Python (sem teste, sem imagem);
  5. readiness do Actuator, IA e Prometheus;
  6. smoke E2E;
  7. **verificação arquitetural em HOMOL**, com os ADRs testados no ambiente implantado.
- **Quais métricas validam os ASRs anteriores?**

  | ADR | Métrica / alerta |
  |---|---|
  | ADR-001 (gatilho para extrair o ranking) | p95 de `/api/forum/engajamento`, alerta `RankingLento` (> 300 ms por 5 min) |
  | ADR-003 (tolerância a falhas da IA) | `gamificacao_recomendacao_total{origem}`, `gamificacao_recomendacao_ia_seconds`, alerta `RecomendacaoDegradada` |
  | ADR-004 (recuperabilidade) | `gamificacao_outbox_pendentes`, `rabbitmq_queue_messages_ready`, alerta `OutboxAcumulando` |
  | ADR-005 (IoT) | `gamificacao_iot_eventos_total{resultado=registrado/duplicata/rejeitado}` |
  | confiabilidade geral | `http_server_requests` 5xx, `up{job="core"}` |

- **Como distinguir métrica técnica de evidência arquitetural?** Métrica técnica descreve o
  sistema (CPU, heap, req/s). **Evidência arquitetural** é uma métrica com uma **hipótese do ADR
  amarrada a ela**: diz o que deveria acontecer e o que significa se não acontecer. Exemplo:
  "broker fora ⇒ `outbox_pendentes` sobe **e** a premiação continua 200 **e** ao voltar zera".
  Se a premiação desse 5xx, o ADR-004 estaria errado. Por isso cada alerta tem o rótulo `adr`.

## Alternativas

| | Deploy independente | Manutenção | Visão do todo |
|---|---|---|---|
| **A — Pipeline único sequencial** (como na AC1) | ❌ qualquer mudança rebuilda e redeploya tudo | ✅ | ✅ |
| **B — Um pipeline por componente** (Jenkinsfile/job por pasta) | ✅ | ❌ 4+ pipelines duplicando preflight, HOMOL e smoke | ❌ ninguém verifica o conjunto |
| **C — Um Jenkinsfile, CI paralelo + deploy por componente** ✅ | ✅ (`--no-deps`) | ✅ | ✅ (verificação arquitetural no conjunto) |

**Observabilidade:** só *health check* (AC1) × **métricas próprias por ASR + alertas
versionados + dashboard provisionado**. Escolhida a segunda. As métricas técnicas automáticas
(JVM, HTTP) continuam disponíveis, mas não substituem as de ASR.

## Decisão

1. Jenkinsfile evoluído (opção C): parâmetros `COMPONENTE` e `VERIFICAR_ARQUITETURA`; stage 2 em
   paralelo por componente; deploy total ou de um componente; health por
   `/actuator/health/readiness`; **stage 10, verificação arquitetural em HOMOL** (ADR-002
   idempotência, ADR-004 outbox esvaziando, ADR-003 IA parada ⇒ degradado, Prometheus coletando).
2. Core com Actuator + Micrometer/Prometheus e 4 métricas de ASR. **RabbitMQ fora do health
   agregado** (`management.health.rabbit.enabled=false`): o broker fora não pode tornar o Core
   "indisponível", porque isso contradiria o ADR-004. Ele é observado pela métrica do outbox.
3. Prometheus (coleta Core + RabbitMQ, 5 regras de alerta com rótulo `adr`) e Grafana (datasource
   e dashboard "B1 - ASRs da Arquitetura" provisionados por arquivo), no compose de dev e de HOMOL.

## Consequências

**Positivas**
- **a observabilidade se pagou no próprio desafio:** o painel de latência da IA mostrou falhas de
  8 s que o smoke test não via (a resposta era HTTP 200). Isso levou à emenda do ADR-003
  (disjuntor + prazo total), e a correção foi para HOMOL por **deploy independente só do Core**,
  sem reiniciar os outros 8 containers;
- um modelo novo da IA vai para HOMOL com `COMPONENTE=ia-recomendacao`, sem rebuild nem restart
  do Core;
- uma violação de fronteira (ArchUnit) ou uma degradação que não acontece (stage 10) **impede**
  o pipeline de ficar verde;
- os gatilhos de revisão dos ADRs passam a ser **observáveis** (alertas), e não promessas.

**Negativas / trade-offs**
- o stage 10 derruba a IA em HOMOL por alguns segundos (aceitável em homologação; não em
  produção, onde seria um teste de caos agendado);
- dois containers a mais (Prometheus e Grafana) e mais portas em HOMOL;
- sem Alertmanager: os alertas aparecem no Prometheus, mas não notificam ninguém (próximo passo);
- os serviços Python ainda não exportam métricas próprias. A latência e a falha da IA são medidas
  **do lado do Core**, que é o lado que importa para o ASR.

## Evidências

[`docs/arquitetura/evidencias/d6/`](../arquitetura/evidencias/d6/)
