# ADR-005 — Dispositivos IoT via MQTT no RabbitMQ já existente (sem segundo broker)

| | |
|---|---|
| **Status** | Aceito |
| **Data** | 2026-11-04 |
| **Desafio** | B1 · Desafio 5 — IoT e MQTT |
| **Relaciona** | ADR-004 (RabbitMQ para eventos internos) |

## Contexto

Laboratórios passam a produzir eventos de **presença, início e conclusão de atividades** (RF06)
por dispositivos de pouco poder computacional e conexão instável. Os eventos precisam entrar na
plataforma **sem acoplar o Core a cada dispositivo**.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **Tolerância à conectividade intermitente** | O dispositivo cai e volta o tempo todo, então exige protocolo com confirmação por mensagem (QoS 1), buffer no dispositivo e fila durável do lado da plataforma. Altera a **comunicação** e a **operação**. |
| **Eficiência de comunicação** | Hardware limitado não sustenta handshake HTTP/TLS por evento, o que pede conexão persistente e mensagem enxuta (MQTT). Altera o **protocolo**. |
| **Interoperabilidade** | Dispositivos de fabricantes diferentes falam MQTT (padrão de fato em IoT), o Core fala AMQP e Java, e o broker faz a ponte. Altera a **integração**: há uma camada anticorrupção no Core. |

Não são drivers neste momento: **escalabilidade** (poucos laboratórios; vira gatilho de revisão),
**disponibilidade** (coberta pela fila durável) e **segurança de integração** (tratada como
trade-off explícito; ver Consequências).

## RPCs

- dispositivos podem ter baixo poder computacional;
- conectividade pode ser intermitente;
- protocolo e broker adicionam componentes operacionais.

## Perguntas do Tech Lead

- **REST é suficiente?** **Não, para este cenário.** Daria certo com dispositivo potente e rede
  boa. Aqui, cada dispositivo precisaria implementar cliente HTTP, TLS por reconexão, retry e
  buffer próprios. O Core também ficaria exposto a todos os dispositivos, com um endpoint público
  recebendo tráfego de hardware instável.
- **O que o MQTT resolve especificamente?** Conexão TCP persistente com keep-alive, **QoS 1**
  (o broker confirma cada mensagem com PUBACK, e o cliente reenvia sem confirmação), sessão
  persistente e pub/sub por tópico. O dispositivo publica num tópico e **não conhece o Core**.
- **Após MQTT, há motivo para outro broker?** **Não.** O RabbitMQ que já operamos (ADR-004) tem
  plugin MQTT oficial: os tópicos MQTT viram routing keys no exchange `amq.topic`. Um Mosquitto
  ao lado do RabbitMQ seria **um broker a mais e uma ponte a mais** para operar, sem resolver nada
  que o plugin não resolva no volume atual.
- **Como impedir duplicidade ou perda relevante?**
  - **perda:** QoS 1 entre dispositivo e broker, **buffer local** no dispositivo enquanto está
    offline (descarregado em ordem ao reconectar) e **fila durável** `iot.eventos-laboratorio`
    enquanto o Core está fora;
  - **duplicidade:** QoS 1 é "pelo menos uma vez", então cada evento tem `eventoId` gerado pelo
    dispositivo (`dispositivo-boot-sequência`) e o Core deduplica (checagem mais UNIQUE no banco);
  - **mensagem inválida:** rejeitada **sem reenfileirar**, para que um dispositivo com defeito não
    trave a fila.

## Alternativas

| | Intermitência | Eficiência | Acoplamento do Core | Custo operacional |
|---|---|---|---|---|
| **A — REST direto** no Core | ❌ cada dispositivo reimplementa retry/buffer | ❌ HTTP + TLS por evento | ❌ Core exposto a cada dispositivo | baixo |
| **B — MQTT (Mosquitto) + consumidor/gateway** | ✅ | ✅ | ✅ | **+1 broker** e ponte para a mensageria interna |
| **B' — MQTT no RabbitMQ existente (plugin) + consumidor no Core** ✅ | ✅ | ✅ | ✅ (Core só vê uma fila) | **nenhum broker novo** |
| **C — MQTT dedicado + integração posterior com mensageria interna** | ✅ | ✅ | ✅ | alto; só com ASR de escala (milhares de dispositivos, QoS 2, cluster MQTT) |

## Decisão

**B'**: dispositivos falam **MQTT 3.1.1, QoS 1** com o **plugin `rabbitmq_mqtt`** do RabbitMQ.

```
dispositivo ──MQTT QoS1──► RabbitMQ :1883  lab/<lab>/dispositivo/<id>/evento
                             │ (plugin MQTT → exchange amq.topic, lab.<lab>.dispositivo.<id>.evento)
                             ▼
                fila durável iot.eventos-laboratorio
                             │ AMQP
                             ▼
     Core · módulo iot: ConsumidorDeEventosIot → TradutorDeMensagemIot (anticorrupção)
                         → EventosDeLaboratorioService (dedup) → tabela evento_laboratorio
```

- **laboratório e dispositivo são lidos do tópico**, não do payload, porque o tópico é o que o
  broker poderá autorizar por ACL;
- o módulo `iot` não é dependência de nenhum fluxo essencial (regra ArchUnit
  `fluxoEssencialNaoDependeDoIot`);
- o núcleo do módulo não conhece MQTT, AMQP nem JSON (regra `nucleoDoIotNaoConheceMqttNemJson`).

## Consequências

**Positivas**
- dispositivo offline: eventos ficam no buffer e entram em ordem ao reconectar (lab: 3 de 3);
- Core offline: a fila segura e o Core processa ao voltar (lab: 3 de 3);
- reentrega do mesmo evento: 1 registro; payload inválido: rejeitado sem travar a fila;
- mensagem ~30% menor que um POST HTTP mínimo, **sem** handshake HTTP/TLS por evento;
- zero componente novo de infraestrutura além de abrir a porta 1883 e ativar o plugin.

**Negativas / trade-offs**
- **segurança de integração** ainda é de laboratório: usuário `guest` e MQTT sem TLS. Para
  produção: credencial por dispositivo, TLS na porta 8883 e ACL de tópico por dispositivo;
- o mesmo broker carrega o tráfego interno e o IoT, então um pico de dispositivos afeta os
  eventos internos. **Gatilho para um broker MQTT dedicado:** milhares de dispositivos, QoS 2 ou
  necessidade de cluster MQTT próprio;
- a fila IoT precisa existir antes de o primeiro dispositivo publicar (hoje ela é declarada pelo
  Core ao conectar). Em produção, deve ir para as *definitions* do broker;
- mensagens rejeitadas são descartadas. O próximo passo é uma *dead-letter queue* para auditoria.

## Evidências

[`docs/arquitetura/evidencias/d5/`](../arquitetura/evidencias/d5/)
