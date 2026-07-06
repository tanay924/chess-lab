<script setup lang="ts">
import { reactive } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const form = reactive({
  username: "",
  password: ""
});

async function submit() {
  await auth.login(form);
  await router.push(String(route.query.redirect ?? "/library"));
}
</script>

<template>
  <section class="auth-layout">
    <form class="auth-panel" @submit.prevent="submit">
      <p class="eyebrow">Account</p>
      <h1>Log in</h1>
      <label for="login-username">Username</label>
      <input id="login-username" v-model="form.username" autocomplete="username" required />
      <label for="login-password">Password</label>
      <input id="login-password" v-model="form.password" autocomplete="current-password" required type="password" />
      <p v-if="auth.error" class="error-text">{{ auth.error }}</p>
      <button type="submit" :disabled="auth.loading">
        {{ auth.loading ? "Logging in..." : "Log in" }}
      </button>
      <p class="muted">
        No account yet?
        <RouterLink class="inline-link" to="/register">Register</RouterLink>
      </p>
    </form>
  </section>
</template>
