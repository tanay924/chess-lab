<script setup lang="ts">
import { computed } from "vue";
import { Chess, type Square } from "chess.js";

const props = withDefaults(defineProps<{
  fen: string;
  interactive?: boolean;
  legalTargets?: Square[];
  selectedSquare?: Square | null;
}>(), {
  interactive: false,
  legalTargets: () => [],
  selectedSquare: null
});

const emit = defineEmits<{
  squareClick: [square: Square];
}>();

const files = ["a", "b", "c", "d", "e", "f", "g", "h"];
const ranks = [8, 7, 6, 5, 4, 3, 2, 1];

const pieces: Record<string, string> = {
  b: String.fromCodePoint(0x265d),
  k: String.fromCodePoint(0x265a),
  n: String.fromCodePoint(0x265e),
  p: String.fromCodePoint(0x265f),
  q: String.fromCodePoint(0x265b),
  r: String.fromCodePoint(0x265c),
  B: String.fromCodePoint(0x2657),
  K: String.fromCodePoint(0x2654),
  N: String.fromCodePoint(0x2658),
  P: String.fromCodePoint(0x2659),
  Q: String.fromCodePoint(0x2655),
  R: String.fromCodePoint(0x2656)
};

const board = computed(() => {
  const chess = new Chess(props.fen);
  return ranks.flatMap((rank) =>
    files.map((file) => {
      const square = `${file}${rank}` as Square;
      const piece = chess.get(square);
      const dark = (files.indexOf(file) + rank) % 2 === 1;
      return {
        dark,
        label: square,
        legalTarget: props.legalTargets.includes(square),
        piece: piece ? pieces[piece.color === "w" ? piece.type.toUpperCase() : piece.type] : "",
        selected: props.selectedSquare === square
      };
    })
  );
});

function selectSquare(square: Square) {
  emit("squareClick", square);
}
</script>

<template>
  <div class="chess-board" aria-label="Chess board">
    <button
      v-if="interactive"
      v-for="square in board"
      :key="square.label"
      type="button"
      class="board-square"
      :class="{ dark: square.dark, selected: square.selected, 'legal-target': square.legalTarget }"
      :aria-label="square.label"
      @click="selectSquare(square.label)"
    >
      <span>{{ square.piece }}</span>
    </button>
    <div
      v-for="square in interactive ? [] : board"
      :key="square.label"
      class="board-square"
      :class="{ dark: square.dark }"
    >
      <span>{{ square.piece }}</span>
    </div>
  </div>
</template>
