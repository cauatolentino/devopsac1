"""Modelo de recomendacao de cursos a partir do perfil de engajamento do aluno.

Versao 1 e heuristica: a recomendacao depende de quanto o aluno escreve topicos
(perfil "autor"), de quanto ajuda colegas (perfil "mentor") e de quantos cursos
ja conquistou. O objetivo do mini-lab do Desafio 3 e testar a FRONTEIRA entre o
Core Java e o servico Python; um modelo treinado pode substituir esta funcao
sem nenhuma mudanca no Core, desde que mantenha o contrato da API.
"""

from dataclasses import dataclass

VERSAO_MODELO = "heuristico-v1"


@dataclass(frozen=True)
class Perfil:
    cursos_ganhos: int
    topicos_escritos: int
    comentarios_ajuda: int

    @property
    def engajamento(self) -> int:
        return self.topicos_escritos + self.comentarios_ajuda


@dataclass(frozen=True)
class Sugestao:
    titulo: str
    motivo: str
    pontuacao: float


def recomendar(perfil: Perfil, limite: int = 3) -> list[Sugestao]:
    sugestoes: list[Sugestao] = []

    if perfil.engajamento == 0:
        sugestoes.append(Sugestao("Primeiros passos no forum", "ainda sem participacao registrada", 0.9))
        sugestoes.append(Sugestao("Como fazer boas perguntas", "estimular o primeiro topico", 0.7))
    else:
        proporcao_ajuda = perfil.comentarios_ajuda / perfil.engajamento
        if proporcao_ajuda >= 0.5:
            sugestoes.append(Sugestao("Didatica para monitores", "ajuda muito os colegas", round(0.6 + proporcao_ajuda * 0.4, 2)))
            sugestoes.append(Sugestao("Comunicacao tecnica", "perfil mentor", 0.6))
        else:
            sugestoes.append(Sugestao("Escrita tecnica", "escreve muitos topicos", round(0.6 + (1 - proporcao_ajuda) * 0.4, 2)))
            sugestoes.append(Sugestao("Documentacao de software", "perfil autor", 0.6))

    if perfil.cursos_ganhos >= 3:
        sugestoes.append(Sugestao("Trilha avancada de arquitetura", "ja conquistou 3 ou mais cursos", 0.8))
    else:
        sugestoes.append(Sugestao("Fundamentos de engenharia de software", "base para as proximas conquistas", 0.5))

    return sorted(sugestoes, key=lambda s: s.pontuacao, reverse=True)[:limite]
