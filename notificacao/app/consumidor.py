"""Servico de notificacao (B1 / Desafio 4) - consome a fila notificacao.premiacao.

Ack manual: a mensagem so sai da fila DEPOIS de processada. Se o servico cair
no meio, o RabbitMQ entrega de novo (e o Notificador descarta a duplicata).
Se o RabbitMQ nao estiver no ar, tenta reconectar a cada poucos segundos.
"""

import logging
import os
import time

import pika

from app.processamento import Notificador

FILA = "notificacao.premiacao"
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("notificacao")


def main() -> None:
    notificador = Notificador()
    parametros = pika.ConnectionParameters(
        host=os.getenv("RABBITMQ_HOST", "localhost"),
        credentials=pika.PlainCredentials(os.getenv("RABBITMQ_USER", "guest"), os.getenv("RABBITMQ_PASSWORD", "guest")),
        heartbeat=30)

    while True:
        try:
            conexao = pika.BlockingConnection(parametros)
            canal = conexao.channel()
            canal.queue_declare(queue=FILA, durable=True)
            canal.basic_qos(prefetch_count=10)

            def ao_receber(ch, metodo, propriedades, corpo):
                texto = notificador.processar(propriedades.message_id, corpo)
                if texto is None:
                    log.info("DUPLICATA descartada eventoId=%s", propriedades.message_id)
                else:
                    log.info("NOTIFICACAO enviada eventoId=%s :: %s", propriedades.message_id, texto)
                ch.basic_ack(delivery_tag=metodo.delivery_tag)

            canal.basic_consume(queue=FILA, on_message_callback=ao_receber)
            log.info("Consumindo a fila %s", FILA)
            canal.start_consuming()
        except (pika.exceptions.AMQPError, OSError) as erro:
            # AMQPError: conexao recusada/perdida/fechada pelo broker.
            # OSError: inclui socket.gaierror - com o broker parado, o nome dele some do DNS da rede Docker.
            log.warning("RabbitMQ indisponivel (%s); nova tentativa em 3 s", erro.__class__.__name__)
            time.sleep(3)


if __name__ == "__main__":
    main()
