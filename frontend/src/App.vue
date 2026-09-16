<script setup lang="ts">
import { onMounted, ref } from 'vue'

interface Engajamento {
  usuarioId: number
  nome: string
  topicos: number
  comentariosAjuda: number
  engajamento: number
}

const dados = ref<Engajamento | null>(null)
const erro = ref('')
const carregando = ref(true)

onMounted(async () => {
  try {
    const resposta = await fetch(
        'http://localhost:8080/api/alunos/1/engajamento'
    )

    if (!resposta.ok) {
      throw new Error('Erro ao consultar a API')
    }

    dados.value = await resposta.json()
  } catch (e) {
    erro.value = 'Não foi possível conectar com a API.'
    console.error(e)
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <main class="dashboard">
    <div class="card">
      <h1>Engajamento do Usuário</h1>

      <p v-if="carregando">Carregando dados...</p>

      <p v-else-if="erro" class="erro">
        {{ erro }}
      </p>

      <div v-else-if="dados">
        <h2>{{ dados.nome }}</h2>

        <div class="dados">
          <div class="item">
            <span>Tópicos</span>
            <strong>{{ dados.topicos }}</strong>
          </div>

          <div class="item">
            <span>Comentários</span>
            <strong>{{ dados.comentariosAjuda }}</strong>
          </div>

          <div class="item destaque">
            <span>Engajamento</span>
            <strong>{{ dados.engajamento }}</strong>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f4f6f8;
}

.card {
  width: 500px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h1 {
  margin-bottom: 30px;
}

h2 {
  margin-bottom: 25px;
}

.dados {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.item {
  flex: 1;
  padding: 20px 10px;
  background: #f0f2f5;
  border-radius: 10px;
}

.item span {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
}

.item strong {
  font-size: 28px;
}

.destaque {
  background: #e8f5e9;
}

.erro {
  color: #d32f2f;
}
</style>