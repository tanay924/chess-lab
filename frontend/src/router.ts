import { createRouter, createWebHistory } from "vue-router";

import GameReviewPage from "./pages/GameReviewPage.vue";
import ImportPage from "./pages/ImportPage.vue";
import LibraryPage from "./pages/LibraryPage.vue";
import LoginPage from "./pages/LoginPage.vue";
import RegisterPage from "./pages/RegisterPage.vue";
import { useAuthStore } from "./stores/auth";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { component: ImportPage, path: "/" },
    { component: LibraryPage, path: "/library" },
    { component: GameReviewPage, path: "/games/:gameId" },
    { component: LoginPage, meta: { public: true }, path: "/login" },
    { component: RegisterPage, meta: { public: true }, path: "/register" }
  ]
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  await auth.ensureSession().catch(() => undefined);
  if (to.meta.public) {
    return auth.isAuthenticated ? "/library" : true;
  }
  if (!auth.isAuthenticated) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  return true;
});
