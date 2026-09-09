# BLUE - refatoração e cobertura 100%

Relatório JaCoCo gerado com `./mvnw verify` (HTML completo em `target/site/jacoco/index.html`).

**Nenhum teste foi alterado nesta etapa** — os mesmos 16 testes escritos no RED continuam
passando. Só o código de produção mudou.

## Cobertura final do pacote `domain`

| Pacote | Classe | Instrucoes | Branches | Linhas | Status |
|---|---|---|---|---|---|
| com.example.grupo12_praticaatdd.domain | Forum | 48/48 (100%) | 2/2 (100%) | 16/16 (100%) | VERDE 100% |
| com.example.grupo12_praticaatdd.domain | Aluno | 30/30 (100%) | 4/4 (100%) | 9/9 (100%) | VERDE 100% |
| com.example.grupo12_praticaatdd.domain | ParticipacaoForum | 43/43 (100%) | 6/6 (100%) | 13/13 (100%) | VERDE 100% |

**0 instruções perdidas, 0 branches perdidos, 0 linhas perdidas.** Sem vermelho e sem amarelo.

## O que foi refatorado

### `Forum.premiarAlunoDoMes()` — de laço imperativo para Stream

Antes (GREEN) — 14 branches, 2 deles impossíveis de cobrir, e um `return null` morto:

```java
if (participacoes == null || participacoes.isEmpty()) {
    return null;
}
ParticipacaoForum vencedora = null;
for (ParticipacaoForum participacao : participacoes) {
    if (vencedora == null) {
        vencedora = participacao;
    } else if (participacao.getEngajamento() > vencedora.getEngajamento()) {
        vencedora = participacao;
    }
}
if (vencedora != null) {
    Aluno premiado = vencedora.getAluno();
    premiado.ganharCurso();
    return premiado;
}
return null;
```

Depois (BLUE) — nenhum branch, nenhuma guarda redundante, nenhum código morto:

```java
private static final Comparator<ParticipacaoForum> POR_ENGAJAMENTO =
        Comparator.comparingInt(ParticipacaoForum::getEngajamento);

public Aluno premiarAlunoDoMes() {
    return participacoes.stream()
            .max(POR_ENGAJAMENTO)
            .map(this::premiar)
            .orElse(null);
}

private Aluno premiar(ParticipacaoForum vencedora) {
    Aluno premiado = vencedora.getAluno();
    premiado.ganharCurso();
    return premiado;
}
```

O caso "fórum sem participação" (cenário 4) passou a ser tratado pelo próprio `Optional.empty()`
do `max()`, em vez de uma guarda explícita.

### `Aluno`

As duas validações duplicadas (`nome == null` e `nome.trim().isEmpty()`, cada uma lançando a
mesma exceção com a mesma mensagem) viraram uma condição só com `isBlank()`, e a mensagem virou
a constante `NOME_OBRIGATORIO`.

### `ParticipacaoForum`

As validações de `topicosEscritos < 0` e `comentariosAjuda < 0` foram unificadas e as mensagens
viraram constantes.

### `getParticipacoes()`

`Collections.unmodifiableList` (que é só uma *view* sobre a lista interna) virou `List.copyOf`,
que devolve uma cópia realmente imutável.

## Trava no build

O `pom.xml` tem uma regra do JaCoCo que **quebra o build** se o pacote `domain` sair de 100% de
linha ou de branch:

```xml
<rule>
    <element>PACKAGE</element>
    <includes><include>com.example.grupo12_praticaatdd.domain</include></includes>
    <limits>
        <limit><counter>LINE</counter><value>COVEREDRATIO</value><minimum>1.00</minimum></limit>
        <limit><counter>BRANCH</counter><value>COVEREDRATIO</value><minimum>1.00</minimum></limit>
    </limits>
</rule>
```

Log do build em `03-blue-testes-passando.txt` (`jacoco:check (check-dominio-100)` + `BUILD SUCCESS`).
