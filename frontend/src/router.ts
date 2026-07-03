import { createRouter, createWebHistory } from "vue-router";

import GameReviewPage from "./pages/GameReviewPage.vue";
import ImportPage from "./pages/ImportPage.vue";
import LibraryPage from "./pages/LibraryPage.vue";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { component: ImportPage, path: "/" },
    { component: LibraryPage, path: "/library" },
    { component: GameReviewPage, path: "/games/:gameId" }
  ]
});
