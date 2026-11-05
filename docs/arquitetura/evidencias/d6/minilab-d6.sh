#!/usr/bin/env bash
# Mini-lab D6: provocar condicoes observaveis no HOMOL e registrar no Prometheus.
# Pre-requisito: HOMOL no ar (docker compose -f docker-compose.homol.yml up -d).
API=http://localhost:8081; PROM=http://localhost:9091; DC="docker compose -f docker-compose.homol.yml"
t() { date +%H:%M:%S; }
q() { curl -s --get "$PROM/api/v1/query" --data-urlencode "query=$1" | grep -oE '"value":\[[0-9.]+,"[^"]*"\]' | sed -E 's/.*,"([^"]*)"\]/\1/' | head -1; }
alertas() { echo "  [$(t)] alertas: $(curl -s "$PROM/api/v1/alerts" | grep -oE '"alertname":"[^"]*"|"state":"[^"]*"' | paste -sd' ' | sed 's/"alertname"://g; s/"state"://g; s/"//g')"; }
rec() { for i in $(seq 1 $1); do curl -s -o /dev/null "$API/api/recomendacoes?aluno=Lab6"; done; }
premiar() { curl -s -X POST -H 'Content-Type: application/json' -d "{\"nomeAluno\":\"Lab6\",\"topicosEscritos\":4,\"comentariosAjuda\":4,\"mesReferencia\":\"$1\"}" $API/api/participacoes >/dev/null
  r=$(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X POST "$API/api/forum/encerrar-mes?mes=$1"); echo "  [$(t)] POST /encerrar-mes?mes=$1 -> HTTP ${r% *} em ${r#* }s"; }
curl -s -X POST -H 'Content-Type: application/json' -d '{"nome":"Lab6"}' $API/api/alunos >/dev/null
ANO=$(( 4000 + $(date +%s) % 5000 ))   # meses unicos por execucao (a premiacao e idempotente)
echo "aguardando 35 s: disjuntor de execucoes anteriores precisa fechar"; sleep 35

echo "## 1. Linha de base: IA no ar, 20 recomendacoes"
rec 20; sleep 12
echo "  [$(t)] recomendacoes por origem: ia=$(q 'sum(gamificacao_recomendacao_total{origem="ia-recomendacao"})') fallback=$(q 'sum(gamificacao_recomendacao_total{origem="catalogo-padrao"}) or vector(0)')"
echo "  [$(t)] latencia max da IA: $(q 'max(gamificacao_recomendacao_ia_seconds_max{resultado="ok"})') s"; alertas

echo; echo "## 2. CONDICAO PROVOCADA: IA fora do ar ($DC stop ia-recomendacao)"
$DC stop ia-recomendacao >/dev/null 2>&1
for ciclo in 1 2 3 4 5 6; do rec 10; sleep 5; done
echo "  [$(t)] % degradado (1 min): $(q '100 * sum(rate(gamificacao_recomendacao_total{origem="catalogo-padrao"}[1m])) / sum(rate(gamificacao_recomendacao_total[1m]))')"
echo "  [$(t)] latencia max de /api/recomendacoes no periodo: $(q 'max(http_server_requests_seconds_max{uri="/api/recomendacoes"})') s   | disjuntor aberto: $(q 'gamificacao_recomendacao_disjuntor_aberto')"
echo "  [$(t)] 5xx do Core no periodo: $(q 'sum(increase(http_server_requests_seconds_count{status=~"5.."}[2m])) or vector(0)')"; alertas
$DC start ia-recomendacao >/dev/null 2>&1

echo; echo "## 3. CONDICAO PROVOCADA: broker fora do ar ($DC stop rabbitmq)"
$DC stop rabbitmq >/dev/null 2>&1
premiar $ANO-01; premiar $ANO-02; premiar $ANO-03; sleep 35
echo "  [$(t)] gamificacao_outbox_pendentes = $(q 'gamificacao_outbox_pendentes')   | Core readiness: $(curl -s $API/actuator/health/readiness)"; alertas
echo "  -> religando o broker"; $DC start rabbitmq >/dev/null 2>&1
for i in $(seq 1 30); do [ "$(q 'gamificacao_outbox_pendentes')" = "0" ] && break; sleep 5; done
echo "  [$(t)] gamificacao_outbox_pendentes = $(q 'gamificacao_outbox_pendentes')"; sleep 10; alertas
echo "  [$(t)] notificacoes enviadas (log do servico notificacao):"; $DC logs notificacao 2>&1 | grep -oE "NOTIFICACAO enviada .*$ANO-0[1-3]" | sed -E 's/eventoId=[^ ]+ :: //; s/^/      /'
