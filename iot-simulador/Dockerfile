# Simulador de dispositivos IoT de laboratorio (B1 / Desafio 5) - publica MQTT QoS 1.
FROM python:3.12-slim AS teste
WORKDIR /srv
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt pytest==8.3.4
COPY simulador ./simulador
COPY tests ./tests
RUN python -m pytest -q tests

FROM python:3.12-slim
WORKDIR /srv
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY --from=teste /srv/simulador ./simulador
ENV PYTHONUNBUFFERED=1
ENTRYPOINT ["python", "-m", "simulador"]
CMD ["--host", "rabbitmq", "--cenario", "normal"]
