<script setup>
defineProps({
  titulo: { type: String, required: true },
  descricao: { type: String, default: '' }
})
</script>

<template>
  <section class="cartao">
    <header>
      <h2>{{ titulo }}</h2>
      <p v-if="descricao">{{ descricao }}</p>
    </header>
    <slot />
  </section>
</template>

<style scoped>
.cartao {
  background: var(--cartao);
  border: 1px solid var(--borda);
  border-radius: 10px;
  padding: 1.1rem 1.25rem 1.25rem;
}

header {
  margin-bottom: 0.9rem;
}

h2 {
  font-size: 1.05rem;
}

p {
  margin: 0;
  font-size: 0.85rem;
  color: var(--texto-suave);
}
</style>
