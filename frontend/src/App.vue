<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";

import { useAuthStore } from "./stores/auth";

const auth = useAuthStore();
const router = useRouter();

onMounted(() => {
  auth.ensureSession().catch(() => undefined);
});

async function logout() {
  await auth.logout().catch(() => undefined);
  await router.push("/login");
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <RouterLink class="brand" to="/">
        <span class="brand-mark">CL</span>
        <span>
          <strong>Chess Lab</strong>
          <small>Local Stockfish study</small>
        </span>
      </RouterLink>
      <div class="topbar-right">
        <nav v-if="auth.isAuthenticated">
          <RouterLink to="/">Import</RouterLink>
          <RouterLink to="/library">Library</RouterLink>
        </nav>
        <nav v-else>
          <RouterLink to="/login">Login</RouterLink>
          <RouterLink to="/register">Register</RouterLink>
        </nav>
        <div v-if="auth.isAuthenticated" class="user-menu">
          <span>{{ auth.username }}</span>
          <button type="button" class="secondary-button compact-button" @click="logout">Logout</button>
        </div>
      </div>
    </header>

    <main>
      <RouterView />
    </main>
  </div>
</template>
