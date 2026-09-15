# Front-end VueJS — Grupo 12

SPA em **Vue 3 + Vite** que consome a API da US1.

## Telas

Uma única página com quatro blocos:

1. **Cadastrar aluno** — `POST /api/alunos`
2. **Registrar participação no fórum** — `POST /api/participacoes`
3. **Ranking de engajamento do mês** — `GET /api/forum/engajamento?mes=AAAA-MM`,
   com o botão *Encerrar o mês e premiar* (`POST /api/forum/encerrar-mes?mes=AAAA-MM`)
4. **Cursos conquistados** — `GET /api/alunos`, mostrando o resultado da gamificação

Os erros da API (400 e 404) são exibidos com a mensagem que o `TratadorDeErros` devolve.

## Rodar em desenvolvimento

Com o Spring Boot rodando em `http://localhost:8080`:

```bash
npm install
npm run dev
```

A aplicação abre em `http://localhost:5173`. O `vite.config.js` encaminha `/api` para o
backend, então não há problema de CORS no desenvolvimento.

## Build de produção

```bash
npm run build      # gera frontend/dist
npm run preview    # serve o build localmente
```

## Via Docker

O `frontend/Dockerfile` faz o build com Node e serve o resultado com nginx, que também
faz proxy de `/api` para o container do backend. Subir tudo com:

```bash
docker compose up --build
```

## Estrutura

```
frontend/
  index.html
  vite.config.js          proxy de /api para o Spring Boot
  nginx.conf              configuração usada na imagem Docker
  src/
    main.js
    App.vue               página principal
    api.js                cliente HTTP da API
    style.css
    components/
      CartaoSecao.vue     cartão reutilizável de cada bloco
      RankingEngajamento.vue  tabela do ranking, destacando o líder
```
