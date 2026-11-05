#!/usr/bin/env bash
# Mini-lab D4: parte SINCRONA (REST encerrar-mes) x parte ASSINCRONA (outbox -> RabbitMQ -> notificacao).
# Core, broker e consumidor em containers na mesma rede Docker (lab-d4), como no docker-compose.
# Uso: JAR_WIN='C:\caminho\app.jar' bash minilab-d4.sh
export MSYS_NO_PATHCONV=1
B=http://localhost:18080
t() { date +%H:%M:%S; }
filas() { echo "  [$(t)] filas: $(docker exec rabbit-lab rabbitmqctl list_queues name messages_ready -q 2>/dev/null | tr '\t\n' '= ' || echo 'broker fora do ar')"; }
notifs() { echo "  [$(t)] notificacoes enviadas pelo consumidor ate agora:"; docker logs notif-lab 2>&1 | grep -E "NOTIFICACAO|DUPLICATA" | sed -E 's/^.*(NOTIFICACAO|DUPLICATA)/      \1/' | cut -c1-150; }
premiar() { # $1 = mes
  curl -s -X POST -H 'Content-Type: application/json' -d "{\"nomeAluno\":\"Carla\",\"topicosEscritos\":12,\"comentariosAjuda\":8,\"mesReferencia\":\"$1\"}" $B/api/participacoes >/dev/null
  resp=$(curl -s -w '\n%{http_code} %{time_total}' -X POST "$B/api/forum/encerrar-mes?mes=$1")
  m=$(echo "$resp" | tail -1); corpo=$(echo "$resp" | head -1)
  echo "  [$(t)] SINCRONO  POST /encerrar-mes?mes=$1 -> HTTP ${m% *} em ${m#* }s :: $(echo "$corpo" | grep -o '"cursosGanhos":[0-9]*')"; }
esperar() { for i in $(seq 1 60); do eval "$1" >/dev/null 2>&1 && return 0; sleep 2; done; return 1; }

docker rm -f core-lab notif-lab rabbit-lab >/dev/null 2>&1; docker network create lab-d4 >/dev/null 2>&1
docker run -d --name rabbit-lab --network lab-d4 rabbitmq:3.13-management >/dev/null
esperar "docker exec rabbit-lab rabbitmq-diagnostics -q check_port_connectivity"
# Achado do lab: nesta maquina (Docker Desktop/Windows) o 1o boot do container RabbitMQ nao fica
# alcancavel pela rede ate um restart. Restart preventivo antes de ligar os clientes.
docker restart rabbit-lab >/dev/null; esperar "docker exec rabbit-lab rabbitmq-diagnostics -q check_port_connectivity"
docker run -d --name notif-lab --network lab-d4 -e RABBITMQ_HOST=rabbit-lab grupo12-notificacao:d4 >/dev/null
docker create --name core-lab --network lab-d4 -p 18080:8080 -e RABBITMQ_HOST=rabbit-lab -e SPRING_JPA_SHOW_SQL=false eclipse-temurin:21-jre java -jar /app.jar >/dev/null
docker cp "$JAR_WIN" core-lab:/app.jar && docker start core-lab >/dev/null
esperar "curl -sf $B/v3/api-docs"
curl -s -X POST -H 'Content-Type: application/json' -d '{"nome":"Carla"}' $B/api/alunos >/dev/null
echo "## 0. Aquecimento (1a conexao Core->broker e consumidor->broker)"
premiar 2026-08; esperar "docker logs notif-lab 2>&1 | grep -q 2026-08" && echo "  [$(t)] ok: evento de aquecimento notificado"

echo; echo "## 1. Tudo no ar"
premiar 2026-09; sleep 4; filas; notifs

echo; echo "## 2. CONSUMIDOR fora do ar (docker stop notif-lab)"
docker stop notif-lab >/dev/null
premiar 2026-10; sleep 4; filas
echo "  -> religando o consumidor"; docker start notif-lab >/dev/null; sleep 6; filas; notifs

echo; echo "## 3. BROKER fora do ar (docker stop rabbit-lab)"
docker stop rabbit-lab >/dev/null
premiar 2026-11; sleep 6; filas
echo "  -> religando o broker"; docker start rabbit-lab >/dev/null; esperar "docker exec rabbit-lab rabbitmq-diagnostics -q check_port_connectivity"; sleep 8; filas; notifs
