# ---------------------------------------------------------------------------
# Servico Python de recomendacao (B1 / Desafio 3)
# Etapa "teste": roda o pytest durante o build - imagem so e gerada se passar.
# ---------------------------------------------------------------------------
FROM python:3.12-slim AS teste
WORKDIR /srv
COPY requirements.txt requirements-dev.txt ./
RUN pip install --no-cache-dir -r requirements-dev.txt
COPY app ./app
COPY tests ./tests
RUN python -m pytest -q tests

# ---------------------------------------------------------------------------
# Imagem final: so dependencias de execucao
# ---------------------------------------------------------------------------
FROM python:3.12-slim
WORKDIR /srv
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY --from=teste /srv/app ./app

EXPOSE 8000
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/saude')"
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
