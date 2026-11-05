"""Servico Python de recomendacao (B1 / Desafio 3).

Contrato consumido pelo Core Spring Boot:
    POST /recomendacoes   -> cursos recomendados para um perfil de engajamento
    GET  /saude           -> verificacao de vida (usada pelo docker compose)

Variaveis para o mini-lab de falha:
    LATENCIA_ARTIFICIAL_MS  atrasa cada resposta (simula modelo lento / sobrecarga)
    FALHA_ARTIFICIAL        se "true", responde 503 (simula modelo fora do ar)
"""

import os
import time

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from app.modelo import VERSAO_MODELO, Perfil, recomendar

app = FastAPI(title="IA - Recomendacao de cursos", version="1.0.0")


class PerfilRequest(BaseModel):
    aluno: str
    cursosGanhos: int = Field(ge=0)
    topicosEscritos: int = Field(ge=0)
    comentariosAjuda: int = Field(ge=0)


class CursoResponse(BaseModel):
    titulo: str
    motivo: str
    pontuacao: float


class RecomendacaoResponse(BaseModel):
    aluno: str
    versaoModelo: str
    cursos: list[CursoResponse]


@app.get("/saude")
def saude() -> dict:
    return {"status": "ok", "versaoModelo": VERSAO_MODELO}


@app.post("/recomendacoes", response_model=RecomendacaoResponse)
def recomendacoes(pedido: PerfilRequest) -> RecomendacaoResponse:
    _aplicar_falhas_artificiais()
    perfil = Perfil(pedido.cursosGanhos, pedido.topicosEscritos, pedido.comentariosAjuda)
    cursos = [CursoResponse(titulo=s.titulo, motivo=s.motivo, pontuacao=s.pontuacao) for s in recomendar(perfil)]
    return RecomendacaoResponse(aluno=pedido.aluno, versaoModelo=VERSAO_MODELO, cursos=cursos)


def _aplicar_falhas_artificiais() -> None:
    latencia = int(os.getenv("LATENCIA_ARTIFICIAL_MS", "0"))
    if latencia > 0:
        time.sleep(latencia / 1000)
    if os.getenv("FALHA_ARTIFICIAL", "false").lower() == "true":
        raise HTTPException(status_code=503, detail="modelo indisponivel (falha artificial)")
