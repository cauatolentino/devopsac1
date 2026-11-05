#!/usr/bin/env bash
# Mini-lab D3: Core (Spring Boot, porta 18080) consumindo a IA (FastAPI em container, porta 8000).
B=http://localhost:18080
rec() { curl -s -o /tmp/r.json -w "%{http_code} %{time_total}" "$B/api/recomendacoes?aluno=Carla" > /tmp/m.txt; read code t < /tmp/m.txt; echo "  GET /api/recomendacoes -> HTTP $code em ${t}s"; echo "  corpo: $(cat /tmp/r.json)"; }
essencial() { r=$(curl -s -o /dev/null -w "%{http_code} %{time_total}" "$B/api/forum/engajamento?mes=2026-09"); p=$(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X POST "$B/api/forum/encerrar-mes?mes=2026-09"); echo "  fluxo essencial: ranking HTTP ${r% *} (${r#* }s) | encerrar-mes HTTP ${p% *} (${p#* }s)"; }
ia() { docker rm -f ia-lab >/dev/null 2>&1; docker run -d --name ia-lab -p 8000:8000 "$@" grupo12-ia-recomendacao:d3 >/dev/null; for i in $(seq 1 30); do curl -s -o /dev/null http://localhost:8000/saude && break; sleep 1; done; }
curl -s -X POST -H 'Content-Type: application/json' -d '{"nome":"Carla"}' $B/api/alunos >/dev/null
curl -s -X POST -H 'Content-Type: application/json' -d '{"nomeAluno":"Carla","topicosEscritos":12,"comentariosAjuda":8,"mesReferencia":"2026-09"}' $B/api/participacoes >/dev/null

echo "## 1. IA no ar"; ia; rec; essencial
echo; echo "## 2. IA interrompida (docker stop) - Core continua rodando, sem restart"; docker stop ia-lab >/dev/null; rec; essencial
echo; echo "## 3. IA lenta (LATENCIA_ARTIFICIAL_MS=5000, timeout de leitura do Core = 1500 ms)"; ia -e LATENCIA_ARTIFICIAL_MS=5000; rec; essencial
echo; echo "## 4. IA respondendo erro 503 (FALHA_ARTIFICIAL=true)"; ia -e FALHA_ARTIFICIAL=true; rec
echo; echo "## 5. IA volta ao ar (novo deploy so do container Python) - recuperacao automatica"; ia; rec
docker rm -f ia-lab >/dev/null
