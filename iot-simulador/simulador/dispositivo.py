"""Dispositivo de laboratorio simulado (B1 / Desafio 5).

Representa um leitor de cracha / sensor com pouco recurso e conexao instavel:
- gera eventos com um eventoId PROPRIO (dispositivo + boot + sequencia), base da deduplicacao;
  o "boot" muda a cada religamento: sem ele, um dispositivo que reinicia repetiria ids e
  eventos NOVOS seriam descartados como duplicata (achado do mini-lab);
- quando esta sem conexao, guarda os eventos num buffer local limitado;
- ao reconectar, descarrega o buffer em ordem (QoS 1 = o broker confirma cada um).
"""

import json
import time
from collections import deque
from dataclasses import dataclass, field
from datetime import datetime, timezone

TIPOS = ("PRESENCA", "INICIO_ATIVIDADE", "CONCLUSAO_ATIVIDADE")


def topico(laboratorio: str, dispositivo: str) -> str:
    return f"lab/{laboratorio}/dispositivo/{dispositivo}/evento"


@dataclass
class Dispositivo:
    laboratorio: str
    nome: str
    capacidade_buffer: int = 100
    boot: int = field(default_factory=lambda: time.time_ns() // 1_000_000)  # ms desde 1970
    sequencia: int = 0
    buffer: deque = field(default_factory=deque)
    descartados_por_buffer_cheio: int = 0

    def novo_evento(self, tipo: str, aluno: str, atividade: str | None = None, quando: datetime | None = None) -> dict:
        if tipo not in TIPOS:
            raise ValueError(f"tipo invalido: {tipo}")
        self.sequencia += 1
        return {
            "eventoId": f"{self.nome}-{self.boot}-{self.sequencia:05d}",
            "tipo": tipo,
            "aluno": aluno,
            "atividade": atividade,
            "ocorridoEm": (quando or datetime.now(timezone.utc)).strftime("%Y-%m-%dT%H:%M:%SZ"),
        }

    def guardar(self, evento: dict) -> None:
        """Sem conexao: guarda no buffer. Buffer cheio descarta o MAIS ANTIGO (e conta)."""
        if len(self.buffer) >= self.capacidade_buffer:
            self.buffer.popleft()
            self.descartados_por_buffer_cheio += 1
        self.buffer.append(evento)

    def descarregar(self) -> list[dict]:
        pendentes = list(self.buffer)
        self.buffer.clear()
        return pendentes

    @property
    def topico(self) -> str:
        return topico(self.laboratorio, self.nome)


def serializar(evento: dict) -> bytes:
    return json.dumps(evento, separators=(",", ":")).encode("utf-8")


def tamanho_mqtt_publish_qos1(topico_mqtt: str, payload: bytes) -> int:
    """Bytes de um PUBLISH MQTT 3.1.1 QoS 1: cabecalho fixo + topico + packet id + payload."""
    restante = 2 + len(topico_mqtt.encode()) + 2 + len(payload)
    bytes_tamanho = 1 if restante < 128 else 2 if restante < 16384 else 3
    return 1 + bytes_tamanho + restante


def tamanho_http_post(host: str, caminho: str, payload: bytes) -> int:
    """Bytes de um POST HTTP/1.1 minimo equivalente (sem TLS, sem cookies, sem auth)."""
    cabecalho = (f"POST {caminho} HTTP/1.1\r\nHost: {host}\r\nContent-Type: application/json\r\n"
                 f"Content-Length: {len(payload)}\r\nConnection: keep-alive\r\n\r\n")
    return len(cabecalho.encode()) + len(payload)
