import json

from app.processamento import Notificador

EVENTO = json.dumps({"tipo": "PremiacaoConcedida", "alunoId": 3, "aluno": "Carla",
                     "mesReferencia": "2026-09", "engajamento": 20, "cursosGanhos": 1}).encode()


def test_monta_a_notificacao_a_partir_do_evento():
    texto = Notificador().processar("evt-1", EVENTO)
    assert texto.startswith("Parabens, Carla!")
    assert "2026-09" in texto


def test_mesmo_evento_entregue_duas_vezes_notifica_uma_vez():
    notificador = Notificador()
    assert notificador.processar("evt-1", EVENTO) is not None
    assert notificador.processar("evt-1", EVENTO) is None
    assert len(notificador.enviadas) == 1


def test_eventos_diferentes_geram_notificacoes_diferentes():
    notificador = Notificador()
    notificador.processar("evt-1", EVENTO)
    notificador.processar("evt-2", EVENTO)
    assert len(notificador.enviadas) == 2
