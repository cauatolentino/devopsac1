<script setup>
defineProps({
  ranking: { type: Array, required: true }
})
</script>

<template>
  <p v-if="ranking.length === 0" class="vazio">
    Nenhuma participação registrada neste mês.
  </p>

  <table v-else>
    <thead>
      <tr>
        <th>#</th>
        <th>Aluno</th>
        <th>Tópicos</th>
        <th>Ajudas</th>
        <th>Engajamento</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(p, indice) in ranking" :key="p.id" :class="{ lider: indice === 0 }">
        <td>{{ indice + 1 }}</td>
        <td>{{ p.nomeAluno }}</td>
        <td>{{ p.topicosEscritos }}</td>
        <td>{{ p.comentariosAjuda }}</td>
        <td class="engajamento">{{ p.engajamento }}</td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.vazio {
  color: var(--texto-suave);
  font-size: 0.9rem;
  margin: 0;
}

.lider td {
  color: var(--ouro);
  font-weight: 600;
}

.engajamento {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}
</style>
