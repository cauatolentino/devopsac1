# Evidência Postgres/H2

## H2
1. Execute a aplicação pelo IntelliJ sem perfil.
2. Abra `/h2-console`.
3. Use `jdbc:h2:mem:gamificacao`, usuário `sa`, senha vazia.
4. Consulte `USUARIOS`, `TOPICOS` e `COMENTARIOS_AJUDA`.

## PostgreSQL
1. `docker compose up --build`.
2. No pgAdmin (`localhost:5050`), conecte no serviço `postgres`.
3. Consulte as tabelas `usuarios`, `topicos` e `comentarios_ajuda`.
4. Tire os prints solicitados pela atividade.
