import { defineStore } from "pinia";

import * as api from "../api";
import type { AuthCredentials, AuthSession } from "../types";

interface AuthState {
  error: string | null;
  loading: boolean;
  session: AuthSession | null;
}

export const useAuthStore = defineStore("auth", {
  state: (): AuthState => ({
    error: null,
    loading: false,
    session: null
  }),
  getters: {
    isAuthenticated: (state) => state.session?.authenticated === true,
    username: (state) => state.session?.username ?? null
  },
  actions: {
    async ensureSession(): Promise<AuthSession> {
      if (this.session) {
        return this.session;
      }
      return this.run(() => api.getSession());
    },
    async login(credentials: AuthCredentials): Promise<AuthSession> {
      return this.run(() => api.login(credentials));
    },
    async register(credentials: AuthCredentials): Promise<AuthSession> {
      return this.run(() => api.register(credentials));
    },
    async logout(): Promise<AuthSession> {
      return this.run(() => api.logout());
    },
    async run(action: () => Promise<AuthSession>): Promise<AuthSession> {
      this.loading = true;
      this.error = null;
      try {
        this.session = await action();
        return this.session;
      } catch (error) {
        this.error = error instanceof Error ? error.message : "Authentication failed";
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
});
