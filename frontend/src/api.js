// Cliente da API do fórum. A base vem de VITE_API_URL quando informada
// (usado no Docker); caso contrário usa /api, que o proxy do Vite encaminha
// para o Spring Boot em http://localhost:8080.
const BASE = import.meta.env.VITE_API_URL || '/api'

async function requisitar(caminho, opcoes = {}) {
  const resposta = await fetch(`${BASE}${caminho}`, {
    headers: { 'Content-Type': 'application/json' },
    ...opcoes
  })

  const corpo = resposta.status === 204 ? null : await resposta.json()

  if (!resposta.ok) {
    throw new Error(corpo?.mensagem || `Erro ${resposta.status}`)
  }
  return corpo
}

export const api = {
  listarAlunos: () => requisitar('/alunos'),

  cadastrarAluno: (nome) =>
    requisitar('/alunos', { method: 'POST', body: JSON.stringify({ nome }) }),

  registrarParticipacao: (participacao) =>
    requisitar('/participacoes', { method: 'POST', body: JSON.stringify(participacao) }),

  engajamentoDoMes: (mes) => requisitar(`/forum/engajamento?mes=${mes}`),

  encerrarMes: (mes) => requisitar(`/forum/encerrar-mes?mes=${mes}`, { method: 'POST' })
}
