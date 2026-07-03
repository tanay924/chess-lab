<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";

import ChessBoard from "../components/ChessBoard.vue";
import { useGamesStore } from "../stores/games";

const route = useRoute();
const games = useGamesStore();
const gameId = computed(() => String(route.params.gameId));

onMounted(async () => {
  await games.fetchGame(gameId.value);
  await games.fetchReport(gameId.value).catch(() => undefined);
});

async function analyze() {
  await games.startAnalysis(gameId.value);
  await games.fetchReport(gameId.value).catch(() => undefined);
}
</script>

<template>
  <section v-if="games.activeGame" class="review-grid">
    <div class="board-panel">
      <ChessBoard :fen="games.activeGame.finalFen" />
    </div>

    <aside class="review-panel">
      <p class="eyebrow">Game review</p>
      <h1>{{ games.activeGame.white }} vs {{ games.activeGame.black }}</h1>
      <p class="muted">{{ games.activeGame.plyCount }} plies / {{ games.activeGame.result }}</p>

      <button type="button" @click="analyze">Run Stockfish analysis</button>

      <div class="analysis-card">
        <span class="label">Analysis status</span>
        <strong>{{ games.report?.status ?? "not started" }}</strong>
        <p v-if="games.report?.message" class="muted">{{ games.report.message }}</p>
      </div>

      <div class="move-list">
        <div v-for="move in games.activeGame.moves" :key="move.fullMoveNumber">
          <span>{{ move.fullMoveNumber }}.</span>
          <strong>{{ move.whiteMove }}</strong>
          <strong v-if="move.blackMove">{{ move.blackMove }}</strong>
        </div>
      </div>
    </aside>
  </section>

  <p v-else class="muted">Loading game...</p>
</template>
