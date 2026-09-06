# GREEN - cobertura de testes

Relatório JaCoCo gerado com `./mvnw verify` (relatório HTML completo em `target/site/jacoco/index.html`).

Todos os 16 testes passaram, mas a cobertura **ainda não está 100% verde** — é exatamente o que
a etapa GREEN prevê: primeiro fazer passar, depois refatorar.

| Pacote | Classe | Instrucoes | Branches | Linhas | Status |
|---|---|---|---|---|---|
| com.example.grupo12_praticaatdd.domain | Forum | 69/71 (97%) | 12/14 (86%) | 20/21 (95%) | VERMELHO (linha nao coberta) |
| com.example.grupo12_praticaatdd.domain | Aluno | 38/38 (100%) | 4/4 (100%) | 12/12 (100%) | VERDE 100% |
| com.example.grupo12_praticaatdd.domain | ParticipacaoForum | 48/48 (100%) | 6/6 (100%) | 15/15 (100%) | VERDE 100% |
| com.example.grupo12_praticaatdd | Grupo12PraticaAtddApplication | 3/8 (38%) | 0/0 (n/a) | 1/3 (33%) | VERMELHO (linha nao coberta) |

## O que está amarelo e vermelho, e por quê

### `Forum.premiarAlunoDoMes()`

```java
if (participacoes == null || participacoes.isEmpty()) {   // AMARELO: participacoes nunca e null
    return null;
}
...
if (vencedora != null) {                                  // AMARELO: nesse ponto nunca e null
    ...
}
return null;                                              // VERMELHO: linha inalcancavel
```

- **Amarelo (2 branches perdidos):** `participacoes == null` e `vencedora != null` são guardas
  defensivas que nenhum teste consegue tornar falsas, porque a lista é `final` e inicializada no
  campo, e o retorno antecipado já tratou o caso de lista vazia.
- **Vermelho (1 linha perdida):** o `return null` do fim nunca é executado, é código morto
  resultante da mesma guarda redundante.

### `Grupo12PraticaAtddApplication`

O método `main()` não é executado pelos testes (o teste de contexto sobe a aplicação pelo
Spring Test, não pelo `main`). Essa classe é apenas o *bootstrap* do Spring Boot, não tem regra de
negócio, e será excluída do relatório na etapa BLUE.

## Próximo passo

Etapa **BLUE**: refatorar `Forum` removendo as guardas redundantes e o código morto, sem alterar
nenhum teste, até o relatório ficar 100% verde.
