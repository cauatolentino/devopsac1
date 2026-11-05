# Servico de notificacao (B1 / Desafio 4): consumidor RabbitMQ.
# Etapa de teste roda o pytest; a imagem final so existe se os testes passarem.
FROM python:3.12-slim AS teste
WORKDIR /srv
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt pytest==8.3.4
COPY app ./app
COPY tests ./tests
RUN python -m pytest -q tests

FROM python:3.12-slim
WORKDIR /srv
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY --from=teste /srv/app ./app
ENV PYTHONUNBUFFERED=1
CMD ["python", "-m", "app.consumidor"]
