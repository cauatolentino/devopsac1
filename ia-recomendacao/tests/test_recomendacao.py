from fastapi.testclient import TestClient

from app.main import app
from app.modelo import Perfil, recomendar

cliente = TestClient(app)


def test_saude_informa_versao_do_modelo():
    resposta = cliente.get("/saude")
    assert resposta.status_code == 200
    assert resposta.json()["versaoModelo"] == "heuristico-v1"


def test_aluno_sem_participacao_recebe_cursos_de_entrada():
    titulos = [s.titulo for s in recomendar(Perfil(0, 0, 0))]
    assert titulos[0] == "Primeiros passos no forum"


def test_aluno_que_mais_ajuda_recebe_perfil_mentor():
    titulos = [s.titulo for s in recomendar(Perfil(0, 2, 8))]
    assert "Didatica para monitores" in titulos


def test_aluno_que_mais_escreve_recebe_perfil_autor():
    titulos = [s.titulo for s in recomendar(Perfil(0, 12, 3))]
    assert "Escrita tecnica" in titulos


def test_aluno_com_tres_cursos_recebe_trilha_avancada():
    titulos = [s.titulo for s in recomendar(Perfil(3, 5, 5))]
    assert "Trilha avancada de arquitetura" in titulos


def test_contrato_http_consumido_pelo_core():
    resposta = cliente.post("/recomendacoes", json={
        "aluno": "Carla", "cursosGanhos": 1, "topicosEscritos": 12, "comentariosAjuda": 8})
    corpo = resposta.json()
    assert resposta.status_code == 200
    assert corpo["aluno"] == "Carla"
    assert corpo["versaoModelo"] == "heuristico-v1"
    assert 1 <= len(corpo["cursos"]) <= 3
    assert {"titulo", "motivo", "pontuacao"} <= corpo["cursos"][0].keys()


def test_quantidade_negativa_e_rejeitada():
    resposta = cliente.post("/recomendacoes", json={
        "aluno": "Ana", "cursosGanhos": 0, "topicosEscritos": -1, "comentariosAjuda": 0})
    assert resposta.status_code == 422


def test_falha_artificial_responde_503(monkeypatch):
    monkeypatch.setenv("FALHA_ARTIFICIAL", "true")
    resposta = cliente.post("/recomendacoes", json={
        "aluno": "Ana", "cursosGanhos": 0, "topicosEscritos": 1, "comentariosAjuda": 1})
    assert resposta.status_code == 503
