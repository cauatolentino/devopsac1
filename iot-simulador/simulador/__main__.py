"""Roteiros do mini-lab do Desafio 5.

    python -m simulador --host <broker> --cenario normal|intermitente|duplicado|invalido|tamanho

normal       : 1 dispositivo publica presenca -> inicio -> conclusao (QoS 1)
intermitente : conecta, publica 1, CAI a conexao, gera 3 eventos offline (buffer), reconecta e descarrega
duplicado    : publica um evento e reenvia o MESMO eventoId (como uma reentrega do QoS 1)
invalido     : publica um payload fora do contrato (o Core deve rejeitar sem travar a fila)
tamanho      : compara bytes de um evento via MQTT x via HTTP POST
"""

import argparse
import sys
import time

import paho.mqtt.client as mqtt

from simulador.dispositivo import Dispositivo, serializar, tamanho_http_post, tamanho_mqtt_publish_qos1


def conectar(host: str, cliente_id: str) -> mqtt.Client:
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=cliente_id, clean_session=False)
    cliente.username_pw_set("guest", "guest")
    cliente.connect(host, 1883, keepalive=30)
    cliente.loop_start()
    return cliente


def publicar(cliente: mqtt.Client, dispositivo: Dispositivo, evento: dict, rotulo: str = "") -> None:
    info = cliente.publish(dispositivo.topico, serializar(evento), qos=1)
    info.wait_for_publish(timeout=10)
    status = "PUBACK recebido" if info.is_published() else "SEM confirmacao"
    print(f"  publicado {evento['eventoId']} {evento['tipo']:<20} -> {dispositivo.topico}  [{status}] {rotulo}")


def desconectar(cliente: mqtt.Client) -> None:
    cliente.loop_stop()
    cliente.disconnect()


def main() -> int:
    argumentos = argparse.ArgumentParser()
    argumentos.add_argument("--host", default="localhost")
    argumentos.add_argument("--cenario", required=True)
    argumentos.add_argument("--aluno", default="Carla")
    a = argumentos.parse_args()

    dispositivo = Dispositivo(laboratorio="lab-redes", nome="leitor-07")

    if a.cenario == "tamanho":
        evento = dispositivo.novo_evento("CONCLUSAO_ATIVIDADE", a.aluno, "Pratica de sub-redes")
        corpo = serializar(evento)
        mqtt_bytes = tamanho_mqtt_publish_qos1(dispositivo.topico, corpo)
        http_bytes = tamanho_http_post("core.plataforma.edu", "/api/iot/eventos", corpo)
        print(f"  payload JSON: {len(corpo)} bytes")
        print(f"  MQTT PUBLISH QoS1: {mqtt_bytes} bytes (conexao TCP mantida aberta; PUBACK de 4 bytes)")
        print(f"  HTTP/1.1 POST minimo: {http_bytes} bytes (+ resposta HTTP, + handshake TLS a cada reconexao)")
        print(f"  economia por evento: {100 * (http_bytes - mqtt_bytes) / http_bytes:.0f}%")
        return 0

    cliente = conectar(a.host, f"sim-{dispositivo.nome}")
    try:
        if a.cenario == "normal":
            for tipo, atividade in (("PRESENCA", None), ("INICIO_ATIVIDADE", "Pratica de sub-redes"),
                                    ("CONCLUSAO_ATIVIDADE", "Pratica de sub-redes")):
                publicar(cliente, dispositivo, dispositivo.novo_evento(tipo, a.aluno, atividade))

        elif a.cenario == "intermitente":
            dispositivo.sequencia = 100
            publicar(cliente, dispositivo, dispositivo.novo_evento("PRESENCA", a.aluno), "(online)")
            desconectar(cliente)
            print("  !! conexao perdida - eventos vao para o buffer local")
            for tipo in ("INICIO_ATIVIDADE", "CONCLUSAO_ATIVIDADE", "PRESENCA"):
                evento = dispositivo.novo_evento(tipo, a.aluno, "Pratica de VLAN")
                dispositivo.guardar(evento)
                print(f"  bufferizado {evento['eventoId']} {evento['tipo']} (buffer={len(dispositivo.buffer)})")
                time.sleep(1)
            cliente = conectar(a.host, f"sim-{dispositivo.nome}")
            print(f"  >> reconectado - descarregando {len(dispositivo.buffer)} evento(s) em ordem")
            for evento in dispositivo.descarregar():
                publicar(cliente, dispositivo, evento, "(do buffer)")

        elif a.cenario == "duplicado":
            dispositivo.sequencia = 200
            evento = dispositivo.novo_evento("PRESENCA", a.aluno)
            publicar(cliente, dispositivo, evento, "(original)")
            publicar(cliente, dispositivo, evento, "(REENVIO do mesmo eventoId)")

        elif a.cenario == "invalido":
            info = cliente.publish(dispositivo.topico, b'{"tipo":"TELEPORTE"}', qos=1)
            info.wait_for_publish(timeout=10)
            print(f"  publicado payload invalido -> {dispositivo.topico}")
            dispositivo.sequencia = 300
            publicar(cliente, dispositivo, dispositivo.novo_evento("PRESENCA", a.aluno), "(valido, logo depois)")
        else:
            print(f"cenario desconhecido: {a.cenario}", file=sys.stderr)
            return 2
    finally:
        desconectar(cliente)
    return 0


if __name__ == "__main__":
    sys.exit(main())
