# Evidências da entrega — Grupo 12

Tudo aqui foi gerado executando o projeto de verdade (saída de terminal, relatórios do build e
capturas de tela dos serviços rodando). Nada foi montado à mão.

## 1. TDD — ciclo RED / GREEN / BLUE

| Etapa | Resultado | Arquivo |
|---|---|---|
| **RED** | 16 testes, **11 falhas + 2 erros**, BUILD FAILURE | [`tdd/01-red-testes-falhando.txt`](tdd/01-red-testes-falhando.txt) |
| **GREEN** | 16/16 passando, cobertura **com amarelo e vermelho** | [`tdd/02-green-testes-passando.txt`](tdd/02-green-testes-passando.txt) · [`tdd/02-green-cobertura.md`](tdd/02-green-cobertura.md) |
| **BLUE** | 16/16 passando, **100% sem amarelo e sem vermelho** | [`tdd/03-blue-testes-passando.txt`](tdd/03-blue-testes-passando.txt) · [`tdd/03-blue-cobertura.md`](tdd/03-blue-cobertura.md) |
| **Suíte final** | **46 testes**, 100% de cobertura no projeto inteiro | [`tdd/04-suite-completa.txt`](tdd/04-suite-completa.txt) |

Relatórios HTML do JaCoCo (abrir no navegador):

- [`tdd/jacoco-blue/index.html`](tdd/jacoco-blue/index.html) — fim da etapa BLUE (só o domínio)
- [`tdd/jacoco-final/index.html`](tdd/jacoco-final/index.html) — projeto completo, todos os pacotes em 100%

Detalhes e rastreabilidade BDD → teste → autor: [`tdd/README.md`](tdd/README.md).

## 2. BDD — Cucumber

- [`bdd/cucumber-report.html`](bdd/cucumber-report.html) — relatório dos **8 cenários / 29 passos**,
  todos passando
- Arquivo Gherkin: [`src/test/resources/features/premiacao_forum.feature`](../../src/test/resources/features/premiacao_forum.feature)

## 3. API REST

- [`api/01-api-via-docker-postgres.txt`](api/01-api-via-docker-postgres.txt) — todos os endpoints
  chamados com `curl` contra a aplicação rodando em Docker sobre PostgreSQL, incluindo os três
  cenários da US1 e os erros 400/404
- [`api/02-openapi.json`](api/02-openapi.json) — contrato OpenAPI 3.1 gerado pelo springdoc

## 4. Bancos de dados

- [`banco/01-postgres-via-docker.txt`](banco/01-postgres-via-docker.txt) — `psql` dentro do
  container: versão do PostgreSQL, tabelas criadas pelo Hibernate, estrutura das tabelas e os
  dados gravados pela API (Carla com `cursos_ganhos = 1`)
- [`banco/02-h2-em-memoria.txt`](banco/02-h2-em-memoria.txt) — a mesma aplicação no perfil `h2`,
  com o DDL gerado no H2 e a API respondendo igual

## 5. Docker

- [`docker/01-docker-compose-rodando.txt`](docker/01-docker-compose-rodando.txt) — `docker compose ps`,
  imagens construídas, código HTTP de cada serviço exposto e os logs de conexão backend ↔ PostgreSQL

## 6. Capturas de tela

| Tela | Arquivo |
|---|---|
| Swagger UI com os 5 endpoints | [`screenshots/01-swagger-ui.jpg`](screenshots/01-swagger-ui.jpg) |
| Front-end VueJS (formulários) | [`screenshots/02-frontend-vuejs.jpg`](screenshots/02-frontend-vuejs.jpg) |
| Front-end: ranking + cursos conquistados | [`screenshots/03-frontend-ranking-e-premiacao.jpg`](screenshots/03-frontend-ranking-e-premiacao.jpg) |
| Console do H2 com a consulta do ranking | [`screenshots/04-h2-console.jpg`](screenshots/04-h2-console.jpg) |
| PgAdmin conectado ao PostgreSQL do container | [`screenshots/05-pgadmin-postgres.jpg`](screenshots/05-pgadmin-postgres.jpg) |

Em todas as telas o resultado é o mesmo e confirma a regra da US1: **Carla**, com engajamento 20
(12 tópicos + 8 comentários de ajuda), é a única premiada com 1 curso.
