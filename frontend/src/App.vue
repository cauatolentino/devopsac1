<script setup>
import { onMounted, ref } from 'vue'
import { api } from './api'
import CartaoSecao from './components/CartaoSecao.vue'
import RankingEngajamento from './components/RankingEngajamento.vue'

const mes = ref('2026-09')
const alunos = ref([])
const ranking = ref([])
const premiacao = ref(null)
const erro = ref('')
const carregando = ref(false)

const novoAluno = ref('')
const participacao = ref({ nomeAluno: '', topicosEscritos: 0, comentariosAjuda: 0 })

async function executar(acao) {
  erro.value = ''
  carregando.value = true
  try {
    await acao()
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
}

async function atualizar() {
  alunos.value = await api.listarAlunos()
  ranking.value = await api.engajamentoDoMes(mes.value)
}

function cadastrarAluno() {
  executar(async () => {
    await api.cadastrarAluno(novoAluno.value)
    novoAluno.value = ''
    await atualizar()
  })
}

function registrarParticipacao() {
  executar(async () => {
    await api.registrarParticipacao({
      nomeAluno: participacao.value.nomeAluno,
      topicosEscritos: Number(participacao.value.topicosEscritos),
      comentariosAjuda: Number(participacao.value.comentariosAjuda),
      mesReferencia: mes.value
    })
    participacao.value = { nomeAluno: '', topicosEscritos: 0, comentariosAjuda: 0 }
    await atualizar()
  })
}

function encerrarMes() {
  executar(async () => {
    premiacao.value = await api.encerrarMes(mes.value)
    await atualizar()
  })
}

function recarregar() {
  executar(atualizar)
}

onMounted(recarregar)
</script>

<template>
  <div class="pagina">
    <header class="topo">
      <p class="etiqueta">Grupo 12 · Prática ATDD</p>
      <h1>Educação Continuada Gamificada</h1>
      <p class="us">
        <strong>US1:</strong> EU COMO aluno assinante da plataforma QUERO ser o que mais escreve
        tópicos e ajuda outros participantes no fórum PARA ganhar um curso no final do mês.
      </p>

      <div class="mes">
        <div>
          <label for="mes">Mês de referência</label>
          <input id="mes" v-model="mes" placeholder="AAAA-MM" @change="recarregar" />
        </div>
        <button :disabled="carregando" @click="recarregar">Atualizar</button>
      </div>
    </header>

    <p v-if="erro" class="alerta erro">{{ erro }}</p>

    <p v-if="premiacao" class="alerta" :class="premiacao.houvePremiacao ? 'premio' : 'neutro'">
      {{ premiacao.mensagem }}
    </p>

    <main class="grade">
      <CartaoSecao
        titulo="Cadastrar aluno"
        descricao="Aluno assinante que participa do fórum."
      >
        <form @submit.prevent="cadastrarAluno">
          <label for="nome">Nome</label>
          <input id="nome" v-model="novoAluno" placeholder="Ex.: Ana" required />
          <button type="submit" :disabled="carregando">Cadastrar</button>
        </form>
      </CartaoSecao>

      <CartaoSecao
        titulo="Registrar participação no fórum"
        descricao="Engajamento = tópicos escritos + comentários de ajuda."
      >
        <form @submit.prevent="registrarParticipacao">
          <label for="aluno">Aluno</label>
          <input id="aluno" v-model="participacao.nomeAluno" placeholder="Ex.: Ana" required />

          <div class="dupla">
            <div>
              <label for="topicos">Tópicos escritos</label>
              <input id="topicos" v-model="participacao.topicosEscritos" type="number" min="0" />
            </div>
            <div>
              <label for="ajudas">Comentários de ajuda</label>
              <input id="ajudas" v-model="participacao.comentariosAjuda" type="number" min="0" />
            </div>
          </div>

          <button type="submit" :disabled="carregando">Registrar</button>
        </form>
      </CartaoSecao>

      <CartaoSecao
        class="largo"
        titulo="Ranking de engajamento do mês"
        :descricao="`Participações registradas em ${mes}, do maior para o menor engajamento.`"
      >
        <RankingEngajamento :ranking="ranking" />

        <button class="encerrar" :disabled="carregando" @click="encerrarMes">
          Encerrar o mês e premiar o aluno de maior engajamento
        </button>
      </CartaoSecao>

      <CartaoSecao
        class="largo"
        titulo="Cursos conquistados"
        descricao="Resultado da gamificação: cada encerramento de mês dá 1 curso ao líder."
      >
        <table>
          <thead>
            <tr>
              <th>Aluno</th>
              <th>Cursos ganhos</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="aluno in alunos" :key="aluno.id">
              <td>{{ aluno.nome }}</td>
              <td :class="{ premiado: aluno.cursosGanhos > 0 }">{{ aluno.cursosGanhos }}</td>
            </tr>
          </tbody>
        </table>
      </CartaoSecao>
    </main>

    <footer>
      Cauã Tolentino · Pedro Pizzi · Henry Tanaka —
      API em <code>/api</code>, Swagger em <code>/swagger-ui.html</code>
    </footer>
  </div>
</template>

<style scoped>
.pagina {
  max-width: 980px;
  margin: 0 auto;
  padding: 2rem 1.25rem 3rem;
}

.etiqueta {
  margin: 0;
  font-size: 0.75rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--destaque);
}

.topo h1 {
  font-size: 1.8rem;
}

.us {
  max-width: 68ch;
  color: var(--texto-suave);
  font-size: 0.9rem;
}

.mes {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  margin: 1.25rem 0 1.5rem;
}

.mes > div {
  width: 160px;
}

.grade {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.largo {
  grid-column: 1 / -1;
}

form {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

form button {
  margin-top: 0.75rem;
  align-self: flex-start;
}

.dupla {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.encerrar {
  margin-top: 1rem;
  background: var(--ouro);
  color: #422006;
}

.alerta {
  border-radius: 8px;
  padding: 0.7rem 0.9rem;
  margin: 0 0 1rem;
  font-size: 0.9rem;
}

.erro {
  background: #450a0a;
  color: var(--erro);
  border: 1px solid #7f1d1d;
}

.premio {
  background: #422006;
  color: var(--ouro);
  border: 1px solid #854d0e;
}

.neutro {
  background: #1e293b;
  color: var(--texto-suave);
  border: 1px solid var(--borda);
}

.premiado {
  color: var(--ok);
  font-weight: 600;
}

footer {
  margin-top: 2rem;
  font-size: 0.8rem;
  color: var(--texto-suave);
}

code {
  color: var(--destaque);
}
</style>
