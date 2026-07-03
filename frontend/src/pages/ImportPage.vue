<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { Activity, FileText } from "@lucide/vue";

import { previewPgn } from "../lib/pgn";
import { useGamesStore } from "../stores/games";

const router = useRouter();
const games = useGamesStore();
const pgn = ref(`[Event "Casual Game"]
[White "White"]
[Black "Black"]
[Result "1-0"]

1. e4 e5 2. Bc4 Nc6 3. Qh5 Nf6 4. Qxf7# 1-0`);

const preview = computed(() => {
  try {
    return previewPgn(pgn.value);
  } catch {
    return null;
  }
});

async function importGame() {
  if (!preview.value) {
    return;
  }

  const created = await games.createGame({ pgn: pgn.value, ...preview.value });
  await router.push(`/games/${created.id}`);
}
</script>

<template>
  <section class="hero-grid">
    <div class="hero-copy">
      <p class="eyebrow">Local-first Stockfish study</p>
      <h1>Import a game, find the turning points, drill the mistakes.</h1>
      <p>
        Chess Lab is a local training workspace built around async engine analysis, clear review reports,
        and replayable mistake practice.
      </p>
      <div class="feature-row">
        <span><FileText :size="17" /> PGN import</span>
        <span><Activity :size="17" /> Worker analysis</span>
      </div>
    </div>

    <form class="import-panel" @submit.prevent="importGame">
      <label for="pgn">PGN</label>
      <textarea id="pgn" v-model="pgn" spellcheck="false" />

      <div v-if="preview" class="preview-card">
        <strong>{{ preview.white }} vs {{ preview.black }}</strong>
        <span>{{ preview.plyCount }} plies / {{ preview.result }}</span>
      </div>
      <p v-else class="error-text">Paste a complete legal PGN to preview it.</p>

      <button type="submit" :disabled="!preview || games.loading">
        {{ games.loading ? "Importing..." : "Import game" }}
      </button>
      <p v-if="games.error" class="error-text">{{ games.error }}</p>
    </form>
  </section>
</template>
