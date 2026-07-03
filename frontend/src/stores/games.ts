import { defineStore } from "pinia";

import * as api from "../api";
import type { AnalysisJob, AnalysisReport, GameDetail, GameImportPayload, GameSummary } from "../types";

interface GamesState {
  activeGame: GameDetail | null;
  error: string | null;
  games: GameSummary[];
  loading: boolean;
  report: AnalysisReport | null;
}

export const useGamesStore = defineStore("games", {
  state: (): GamesState => ({
    activeGame: null,
    error: null,
    games: [],
    loading: false,
    report: null
  }),
  actions: {
    async createGame(payload: GameImportPayload): Promise<GameDetail> {
      return this.run(async () => {
        const game = await api.createGame(payload);
        this.activeGame = game;
        this.games = [game, ...this.games.filter((existing) => existing.id !== game.id)];
        return game;
      });
    },
    async fetchGame(gameId: string): Promise<GameDetail> {
      return this.run(async () => {
        const game = await api.getGame(gameId);
        this.activeGame = game;
        return game;
      });
    },
    async fetchGames(): Promise<GameSummary[]> {
      return this.run(async () => {
        this.games = await api.listGames();
        return this.games;
      });
    },
    async fetchReport(gameId: string): Promise<AnalysisReport> {
      return this.run(async () => {
        this.report = await api.getReport(gameId);
        return this.report;
      });
    },
    async startAnalysis(gameId: string): Promise<AnalysisJob> {
      return this.run(() => api.startAnalysis(gameId));
    },
    async run<T>(action: () => Promise<T>): Promise<T> {
      this.loading = true;
      this.error = null;
      try {
        return await action();
      } catch (error) {
        this.error = error instanceof Error ? error.message : "Request failed";
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
});
