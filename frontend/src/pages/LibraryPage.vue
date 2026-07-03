<script setup lang="ts">
import { onMounted } from "vue";
import { RouterLink } from "vue-router";

import { useGamesStore } from "../stores/games";

const games = useGamesStore();

onMounted(() => {
  games.fetchGames().catch(() => undefined);
});
</script>

<template>
  <section class="stack">
    <div class="page-heading">
      <p class="eyebrow">Study library</p>
      <h1>Imported games</h1>
    </div>

    <div v-if="games.games.length === 0" class="empty-panel">
      <p>No imported games yet.</p>
      <RouterLink class="button-link" to="/">Import your first PGN</RouterLink>
    </div>

    <div v-else class="game-list">
      <RouterLink v-for="game in games.games" :key="game.id" class="game-row" :to="`/games/${game.id}`">
        <strong>{{ game.white }} vs {{ game.black }}</strong>
        <span>{{ game.plyCount }} plies / {{ game.result }}</span>
      </RouterLink>
    </div>
  </section>
</template>
