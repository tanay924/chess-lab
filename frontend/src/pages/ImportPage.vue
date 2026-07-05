<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { Chess, type Square } from "chess.js";

import ChessBoard from "../components/ChessBoard.vue";
import { buildManualGamePayload, inferManualResult } from "../lib/manualGame";
import { previewPgn, toMoveRecords } from "../lib/pgn";
import { useGamesStore } from "../stores/games";

type ImportMode = "pgn" | "manual";

const router = useRouter();
const games = useGamesStore();
const importMode = ref<ImportMode>("pgn");
const pgn = ref(`[Event "Casual Game"]
[White "White"]
[Black "Black"]
[Result "1-0"]

1. e4 e5 2. Bc4 Nc6 3. Qh5 Nf6 4. Qxf7# 1-0`);

const manualChess = new Chess();
const manualFen = ref(manualChess.fen());
const manualVersion = ref(0);
const selectedSquare = ref<Square | null>(null);

const preview = computed(() => {
  try {
    return previewPgn(pgn.value);
  } catch {
    return null;
  }
});

const legalTargets = computed<Square[]>(() => {
  manualVersion.value;
  if (!selectedSquare.value) {
    return [];
  }
  return manualChess.moves({ square: selectedSquare.value, verbose: true }).map((move) => move.to as Square);
});

const manualMoveRecords = computed(() => {
  manualVersion.value;
  return toMoveRecords(manualChess.history({ verbose: true }));
});

const manualFlatMoves = computed(() => {
  manualVersion.value;
  return manualChess.history();
});

const manualStatus = computed(() => {
  manualVersion.value;
  if (manualChess.isGameOver()) {
    return `Game over / ${inferManualResult(manualChess)}`;
  }
  return manualChess.turn() === "w" ? "White to move" : "Black to move";
});

const manualCanSave = computed(() => manualFlatMoves.value.length > 0);

async function importGame() {
  if (!preview.value) {
    return;
  }

  const created = await games.createGame({ pgn: pgn.value, ...preview.value });
  await router.push(`/games/${created.id}`);
}

async function importManualGame() {
  if (!manualCanSave.value) {
    return;
  }

  const created = await games.createGame(buildManualGamePayload(manualChess, {
    black: "Manual Black",
    result: inferManualResult(manualChess),
    white: "Manual White"
  }));
  await router.push(`/games/${created.id}`);
}

function handleSquareClick(square: Square) {
  if (manualChess.isGameOver()) {
    return;
  }

  const piece = manualChess.get(square);
  if (!selectedSquare.value) {
    if (piece?.color === manualChess.turn()) {
      selectedSquare.value = square;
    }
    return;
  }

  if (selectedSquare.value === square) {
    selectedSquare.value = null;
    return;
  }

  if (tryMove(selectedSquare.value, square)) {
    selectedSquare.value = null;
    syncManualBoard();
    return;
  }

  selectedSquare.value = piece?.color === manualChess.turn() ? square : null;
}

function tryMove(from: Square, to: Square): boolean {
  try {
    return Boolean(manualChess.move({ from, promotion: "q", to }));
  } catch {
    return false;
  }
}

function undoManualMove() {
  manualChess.undo();
  selectedSquare.value = null;
  syncManualBoard();
}

function resetManualGame() {
  manualChess.reset();
  selectedSquare.value = null;
  syncManualBoard();
}

function syncManualBoard() {
  manualFen.value = manualChess.fen();
  manualVersion.value += 1;
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
      <p class="intro-note">PGN import. Manual entry. Local Stockfish review.</p>
    </div>

    <div class="import-panel">
      <div class="mode-tabs" role="tablist" aria-label="Import mode">
        <button
          type="button"
          class="mode-tab"
          :class="{ active: importMode === 'pgn' }"
          @click="importMode = 'pgn'"
        >
          PGN
        </button>
        <button
          type="button"
          class="mode-tab"
          :class="{ active: importMode === 'manual' }"
          @click="importMode = 'manual'"
        >
          Board
        </button>
      </div>

      <form v-if="importMode === 'pgn'" class="import-mode-panel" @submit.prevent="importGame">
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
      </form>

      <form v-else class="import-mode-panel manual-entry" @submit.prevent="importManualGame">
        <div class="manual-board-shell">
          <ChessBoard
            :fen="manualFen"
            interactive
            :legal-targets="legalTargets"
            :selected-square="selectedSquare"
            @square-click="handleSquareClick"
          />
        </div>

        <div class="manual-toolbar">
          <div>
            <span class="label">Manual board</span>
            <strong>{{ manualStatus }}</strong>
          </div>
          <div class="manual-actions">
            <button type="button" class="secondary-button" :disabled="!manualCanSave" @click="undoManualMove">
              Undo
            </button>
            <button type="button" class="secondary-button" @click="resetManualGame">Reset</button>
          </div>
        </div>

        <div v-if="manualMoveRecords.length" class="move-list compact-move-list">
          <div v-for="move in manualMoveRecords" :key="move.fullMoveNumber">
            <span>{{ move.fullMoveNumber }}.</span>
            <strong>{{ move.whiteMove }}</strong>
            <strong v-if="move.blackMove">{{ move.blackMove }}</strong>
          </div>
        </div>
        <p v-else class="muted">Starting position.</p>

        <button type="submit" :disabled="!manualCanSave || games.loading">
          {{ games.loading ? "Saving..." : "Save manual game" }}
        </button>
      </form>

      <p v-if="games.error" class="error-text">{{ games.error }}</p>
    </div>
  </section>
</template>
