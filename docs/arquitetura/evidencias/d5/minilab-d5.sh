#!/usr/bin/env bash
# Mini-lab D5: simulador de dispositivo publica MQTT (QoS 1) no RabbitMQ (plugin MQTT);
# o Core consome a fila iot.eventos-laboratorio (AMQP), traduz e registra o evento.
# Uso: JAR_WIN='C:\...\app.jar' PLUGINS_WIN='C:\...\docker\rabbitmq\enabled_plugins' bash minilab-d5.sh
export MSYS_NO_PATHCONV=1
B=http://localhost:18080
t() { date +%H:%M:%S; }
esperar() { for i in $(seq 1 60); do eval "$1" >/dev/null 2>&1 && return 0; sleep 2; done; return 1; }
sim() { docker run --rm --network lab-d5 grupo12-iot-simulador:d5 --host rabbit-lab "$@"; }
fila() { echo "  [$(t)] fila iot.eventos-laboratorio: $(docker exec rabbit-lab rabbitmqctl list_queues name messages_ready -q 2>/dev/null | grep iot | awk '{print $2}') mensagem(ns) esperando"; }
eventos() { echo "  [$(t)] GET /api/laboratorio/eventos?aluno=$1 -> $(curl -s "$B/api/laboratorio/eventos?aluno=$1" | grep -o '"eventoId":"[^"]*"' | sed 's/"eventoId"://; s/"//g' | tr '\n' ' ')"; }
logcore() { docker logs --since "$1" core-lab 2>&1 | grep -aE "IoT:" | sed -E 's/^.*IoT: /      Core IoT: /' | cut -c1-150; }

docker rm -f core-lab rabbit-lab >/dev/null 2>&1; docker network create lab-d5 >/dev/null 2>&1
docker run -d --name rabbit-lab --network lab-d5 -v "$PLUGINS_WIN:/etc/rabbitmq/enabled_plugins:ro" rabbitmq:3.13-management >/dev/null
esperar "docker exec rabbit-lab rabbitmq-diagnostics -q check_port_connectivity"
docker restart rabbit-lab >/dev/null; esperar "docker exec rabbit-lab rabbitmq-diagnostics -q check_port_connectivity"   # achado do D4
echo "plugins ativos: $(docker exec rabbit-lab rabbitmq-plugins list -e -m | tr '\n' ' ')"
echo "listeners: $(docker exec rabbit-lab rabbitmq-diagnostics -q listeners | grep -oE 'protocol: [a-z/0-9.-]+' | tr '\n' ' ')"
docker create --name core-lab --network lab-d5 -p 18080:8080 -e RABBITMQ_HOST=rabbit-lab -e SPRING_JPA_SHOW_SQL=false eclipse-temurin:21-jre java -jar /app.jar >/dev/null
docker cp "$JAR_WIN" core-lab:/app.jar && docker start core-lab >/dev/null; esperar "curl -sf $B/v3/api-docs"; sleep 3

echo; echo "## 1. Dispositivo online: presenca -> inicio -> conclusao (MQTT QoS 1)"
T=$(date -u +%Y-%m-%dT%H:%M:%SZ); sim --cenario normal; sleep 3; logcore "$T"; eventos Carla

echo; echo "## 2. Conectividade intermitente: dispositivo perde a conexao, bufferiza e descarrega ao voltar"
T=$(date -u +%Y-%m-%dT%H:%M:%SZ); sim --cenario intermitente; sleep 3; logcore "$T"; eventos Carla

echo; echo "## 3. Duplicidade: mesmo eventoId entregue 2x (reentrega do QoS 1)"
T=$(date -u +%Y-%m-%dT%H:%M:%SZ); sim --cenario duplicado; sleep 3; logcore "$T"; eventos Carla

echo; echo "## 3b. Dispositivo RELIGADO (novo boot) publica de novo: eventos novos NAO podem ser confundidos com duplicata"
T=$(date -u +%Y-%m-%dT%H:%M:%SZ); sim --cenario normal --aluno Diego; sim --cenario normal --aluno Diego; sleep 3; logcore "$T"; eventos Diego

echo; echo "## 4. Payload invalido de um dispositivo com defeito"
T=$(date -u +%Y-%m-%dT%H:%M:%SZ); sim --cenario invalido; sleep 3; logcore "$T"; fila

echo; echo "## 5. CORE fora do ar: dispositivos continuam publicando; a fila segura"
docker stop core-lab >/dev/null; sim --cenario normal --aluno Bruno; sleep 2; fila
echo "  -> religando o Core (H2 em memoria: so os eventos da fila vao reaparecer)"; T=$(date -u +%Y-%m-%dT%H:%M:%SZ); docker start core-lab >/dev/null; esperar "curl -sf $B/v3/api-docs"; sleep 4; fila; logcore "$T"; eventos Bruno

echo; echo "## 6. Eficiencia de comunicacao (mesmo evento)"; docker run --rm grupo12-iot-simulador:d5 --cenario tamanho
