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
  await auth.register(form);
  await router.push(String(route.query.redirect ?? "/library"));
}
</script>

<template>
  <section class="auth-layout">
    <form class="auth-panel" @submit.prevent="submit">
      <p class="eyebrow">Account</p>
      <h1>Register</h1>
      <label for="register-username">Username</label>
      <input id="register-username" v-model="form.username" autocomplete="username" required />
      <label for="register-password">Password</label>
      <input
        id="register-password"
        v-model="form.password"
        autocomplete="new-password"
        minlength="8"
        required
        type="password"
      />
      <p v-if="auth.error" class="error-text">{{ auth.error }}</p>
      <button type="submit" :disabled="auth.loading">
        {{ auth.loading ? "Registering..." : "Register" }}
      </button>
      <p class="muted">
        Already have an account?
        <RouterLink class="inline-link" to="/login">Log in</RouterLink>
      </p>
    </form>
  </section>
</template>
