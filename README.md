# Grupo 12 - Prática ATDD

Trabalho da disciplina de Engenharia de Software - prática de **ATDD** (Acceptance Test Driven
Development) sobre o estudo de caso **Educação Continuada Gamificada**.

## Integrantes

| Integrante | US redigida |
|---|---|
| Cauã Tolentino | US1 |
| Pedro Pizzi | US2 |
| Henry Tanaka | US3 |

## Visão do Produto

> Para quem busca se qualificar continuamente, a plataforma de Educação Continuada Gamificada
> oferece cursos online e EAD por assinatura, recompensando o aproveitamento e o engajamento no
> fórum com novos cursos, plano Premium, vouchers e moedas.

## User Stories

### US1 - Premiação por engajamento no fórum *(autor: Cauã Tolentino)*
```
EU COMO aluno assinante da plataforma
QUERO ser o que mais escreve tópicos e ajuda outros participantes no fórum
PARA ganhar um curso no final do mês.
```

### US2 - Premiação por aproveitamento *(autor: Pedro Pizzi)*
```
EU COMO aluno assinante da plataforma
QUERO concluir um curso com média acima de 7,0
PARA ganhar o direito de realizar mais 3 cursos.
```

### US3 - Evolução para plano Premium *(autor: Henry Tanaka)*
```
EU COMO aluno assinante da plataforma
QUERO acumular 12 cursos conquistados
PARA ter meu plano atualizado para Premium com voucher e 3 moedas.
```

## US escolhida para implementação

**US1 - Premiação por engajamento no fórum**, redigida por Cauã Tolentino.

Motivo da escolha: é a US com a regra de negócio mais rica para o ciclo TDD (cálculo de
engajamento, comparação entre alunos e cenário de exceção quando não há participação), e as
outras duas dependem dela para fazer sentido no fluxo de gamificação.

## Planilha ATDD

A planilha usada na primeira entrega está versionada em [`Grupo12_ATDD.xlsx`](Grupo12_ATDD.xlsx).

## Stack

- Java 17 / Spring Boot 4.1.1
- Spring Web (MVC), Spring Data JPA
- H2 (desenvolvimento) e PostgreSQL (execução via Docker)
- JUnit 5 para o TDD

## Como rodar

```bash
./mvnw spring-boot:run
```
