import json
from datetime import datetime, timezone

import pytest

from simulador.dispositivo import Dispositivo, serializar, tamanho_http_post, tamanho_mqtt_publish_qos1


def test_evento_tem_id_proprio_sequencial_e_formato_do_contrato():
    d = Dispositivo("lab-redes", "leitor-07", boot=1730725200)
    e1 = d.novo_evento("PRESENCA", "Carla", quando=datetime(2026, 11, 4, 13, 0, tzinfo=timezone.utc))
    e2 = d.novo_evento("CONCLUSAO_ATIVIDADE", "Carla", "Sub-redes")
    assert e1["eventoId"] == "leitor-07-1730725200-00001"
    assert e2["eventoId"] == "leitor-07-1730725200-00002"
    assert e1["ocorridoEm"] == "2026-11-04T13:00:00Z"
    assert d.topico == "lab/lab-redes/dispositivo/leitor-07/evento"


def test_tipo_fora_do_contrato_e_recusado_no_dispositivo():
    with pytest.raises(ValueError):
        Dispositivo("lab", "d").novo_evento("TELEPORTE", "Ana")


def test_buffer_offline_preserva_ordem():
    d = Dispositivo("lab", "d")
    for i in range(3):
        d.guardar(d.novo_evento("PRESENCA", f"aluno{i}"))
    assert [e["aluno"] for e in d.descarregar()] == ["aluno0", "aluno1", "aluno2"]
    assert len(d.buffer) == 0


def test_buffer_cheio_descarta_o_mais_antigo_e_conta():
    d = Dispositivo("lab", "d", capacidade_buffer=2)
    for i in range(3):
        d.guardar(d.novo_evento("PRESENCA", f"aluno{i}"))
    assert [e["aluno"] for e in d.descarregar()] == ["aluno1", "aluno2"]
    assert d.descartados_por_buffer_cheio == 1


def test_mqtt_e_bem_menor_que_http_para_o_mesmo_evento():
    d = Dispositivo("lab-redes", "leitor-07")
    corpo = serializar(d.novo_evento("PRESENCA", "Carla"))
    assert json.loads(corpo)["aluno"] == "Carla"
    assert tamanho_mqtt_publish_qos1(d.topico, corpo) < tamanho_http_post("core.plataforma.edu", "/api/iot/eventos", corpo)


def test_dispositivo_religado_nao_repete_ids_do_boot_anterior():
    antes = Dispositivo("lab", "leitor-07", boot=1).novo_evento("PRESENCA", "Ana")
    depois = Dispositivo("lab", "leitor-07", boot=2).novo_evento("PRESENCA", "Ana")
    assert antes["eventoId"] != depois["eventoId"]
