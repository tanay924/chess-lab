<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";

import ChessBoard from "../components/ChessBoard.vue";
import { buildReplayPositions } from "../lib/replay";
import { useGamesStore } from "../stores/games";

const route = useRoute();
const games = useGamesStore();
const gameId = computed(() => String(route.params.gameId));
const isAnalyzing = computed(() => games.report?.status === "queued" || games.report?.status === "running");
const replayIndex = ref(0);
const replayPositions = computed(() => games.activeGame ? buildReplayPositions(games.activeGame) : []);
const currentReplayPosition = computed(() => replayPositions.value[replayIndex.value] ?? replayPositions.value[0]);
const canStepBack = computed(() => replayIndex.value > 0);
const canStepForward = computed(() => replayIndex.value < replayPositions.value.length - 1);

onMounted(async () => {
  await games.fetchGame(gameId.value);
  replayIndex.value = 0;
  const report = await games.fetchReport(gameId.value).catch(() => undefined);
  if (report?.status === "queued" || report?.status === "running") {
    await pollReport();
  }
});

watch(replayPositions, (positions) => {
  if (replayIndex.value >= positions.length) {
    replayIndex.value = 0;
  }
});

async function analyze() {
  await games.startAnalysis(gameId.value);
  await pollReport();
}

function stepReplay(delta: number) {
  replayIndex.value = Math.min(Math.max(replayIndex.value + delta, 0), replayPositions.value.length - 1);
}

async function pollReport() {
  for (let attempt = 0; attempt < 300; attempt += 1) {
    const report = await games.fetchReport(gameId.value).catch(() => undefined);
    if (report?.status === "ready" || report?.status === "failed") {
      return;
    }
    await new Promise((resolve) => window.setTimeout(resolve, 1000));
  }
}
</script>

<template>
  <section v-if="games.activeGame" class="review-grid">
    <div class="board-panel">
      <ChessBoard :fen="currentReplayPosition?.fen ?? games.activeGame.finalFen" />
      <div class="replay-controls">
        <button type="button" class="secondary-button" :disabled="!canStepBack" @click="stepReplay(-1)">
          Back
        </button>
        <div>
          <span class="label">Position</span>
          <strong>{{ currentReplayPosition?.label ?? "Start" }}</strong>
          <span class="muted">{{ replayIndex }} / {{ replayPositions.length - 1 }}</span>
        </div>
        <button type="button" class="secondary-button" :disabled="!canStepForward" @click="stepReplay(1)">
          Next
        </button>
      </div>
    </div>

    <aside class="review-panel">
      <p class="eyebrow">Game review</p>
      <h1>{{ games.activeGame.white }} vs {{ games.activeGame.black }}</h1>
      <p class="muted">{{ games.activeGame.plyCount }} plies / {{ games.activeGame.result }}</p>

      <button type="button" :disabled="isAnalyzing" @click="analyze">
        {{ isAnalyzing ? "Analyzing..." : "Run Stockfish analysis" }}
      </button>

      <div class="analysis-card">
        <span class="label">Analysis status</span>
        <strong>{{ games.report?.status ?? "not started" }}</strong>
        <p v-if="games.report?.message" class="muted">{{ games.report.message }}</p>
        <div v-if="games.report?.evaluations.length" class="evaluation-list">
          <div v-for="evaluation in games.report.evaluations" :key="evaluation.ply">
            <span>{{ evaluation.ply }}</span>
            <strong>{{ evaluation.playedMove }}</strong>
            <span>{{ evaluation.classification }}</span>
            <span>Best: {{ evaluation.bestMove }}</span>
          </div>
        </div>
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
