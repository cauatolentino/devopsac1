# language: pt
@us1
Funcionalidade: Premiação do aluno com maior engajamento no fórum

  US1 (autor: Cauã Tolentino)
  EU COMO aluno assinante da plataforma
  QUERO ser o que mais escreve tópicos e ajuda outros participantes no fórum
  PARA ganhar um curso no final do mês.

  @cenario1 @caua-tolentino
  Cenário: Premiar o aluno com maior engajamento
    # BDD escrito por Cauã Tolentino
    Dado um fórum com a participação de vários alunos no mês
      | aluno | topicos | comentarios |
      | Ana   |      10 |           5 |
      | Bruno |       3 |           4 |
    E com engajamentos diferentes
    Quando o mês é encerrado
    Então o aluno "Ana" deve ser premiado
    E "Ana" deve ter 1 curso ganho

  @cenario2 @pedro-pizzi
  Cenário: Calcular o engajamento de um aluno
    # BDD escrito por Pedro Pizzi
    Dado que o aluno "Ana" escreveu 10 tópicos e fez 5 comentários de ajuda
    Quando o engajamento dele é calculado
    Então o engajamento deve ser 15

  @cenario3 @henry-tanaka
  Cenário: Somente o aluno de maior engajamento é premiado
    # BDD escrito por Henry Tanaka
    Dado um fórum com a participação de vários alunos no mês
      | aluno | topicos | comentarios |
      | Bruno |       3 |           4 |
      | Ana   |       2 |           1 |
      | Carla |      12 |           8 |
    Quando o mês é encerrado
    Então o aluno "Carla" deve ser premiado
    E "Carla" deve ter 1 curso ganho
    E "Ana" deve ter 0 cursos ganhos
    E "Bruno" deve ter 0 cursos ganhos

  @cenario4 @caua-tolentino
  Cenário: Mês sem nenhuma participação não premia ninguém
    # BDD escrito por Cauã Tolentino
    Dado um fórum sem nenhuma participação no mês
    Quando o mês é encerrado
    Então nenhum aluno deve ser premiado

  @regras
  Esquema do Cenário: O engajamento é sempre a soma de tópicos e comentários de ajuda
    Dado que o aluno "<aluno>" escreveu <topicos> tópicos e fez <comentarios> comentários de ajuda
    Quando o engajamento dele é calculado
    Então o engajamento deve ser <engajamento>

    Exemplos:
      | aluno | topicos | comentarios | engajamento |
      | Ana   |      10 |           5 |          15 |
      | Bruno |       3 |           4 |           7 |
      | Carla |      12 |           8 |          20 |
      | Diego |       0 |           0 |           0 |
