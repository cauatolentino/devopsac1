"""Regra do consumidor de notificacoes (RF05), separada do codigo de rede para ser testavel.

Entrega e "pelo menos uma vez" (o relay do outbox pode reenviar o mesmo evento),
entao o consumidor e IDEMPOTENTE: descarta mensagens cujo eventoId ja foi visto.
"""

import json
from dataclasses import dataclass, field


@dataclass
class Notificador:
    vistos: set[str] = field(default_factory=set)
    enviadas: list[str] = field(default_factory=list)

    def processar(self, evento_id: str | None, corpo: bytes) -> str | None:
        """Retorna o texto da notificacao enviada, ou None se for duplicata."""
        if evento_id and evento_id in self.vistos:
            return None
        evento = json.loads(corpo.decode("utf-8"))
        texto = (f"Parabens, {evento['aluno']}! Voce foi o aluno mais engajado do forum em "
                 f"{evento['mesReferencia']} e ganhou 1 curso (total: {evento['cursosGanhos']}).")
        if evento_id:
            self.vistos.add(evento_id)
        self.enviadas.append(texto)
        return texto
