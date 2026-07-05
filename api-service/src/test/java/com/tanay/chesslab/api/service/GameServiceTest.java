package com.tanay.chesslab.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.MoveEvaluation;
import com.tanay.chesslab.api.domain.MoveRecord;

class GameServiceTest {

	private final GameService games = new GameService(game -> List.of(), Runnable::run);

	@Test
	void createsAndListsImportedGame() {
		CreateGameRequest request = new CreateGameRequest(
				"[White \"Ada\"]\n[Black \"Grace\"]\n\n1. e4 e5",
				"Ada",
				"Grace",
				"*",
				2,
				"rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
				List.of(new MoveRecord(1, "e4", "e5",
						"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
						"rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
						"e2e4",
						"e7e5")));

		var created = games.createGame(request);

		assertThat(created.id()).isEqualTo("1");
		assertThat(created.white()).isEqualTo("Ada");
		assertThat(created.black()).isEqualTo("Grace");
		assertThat(created.moves()).hasSize(1);
		assertThat(games.listGames()).extracting("id").containsExactly("1");
	}

	@Test
	void marksAnalysisFailedWhenEngineCannotRun() {
		GameService failingGames = new GameService(game -> {
			throw new IllegalStateException("Stockfish is missing");
		}, Runnable::run);
		var created = failingGames.createGame(new CreateGameRequest(
				"1. e4 e5",
				"White",
				"Black",
				"*",
				2,
				null,
				List.of(new MoveRecord(1, "e4", "e5", null, null, null, null))));

		var job = failingGames.startAnalysis(created.id());
		var report = failingGames.getReport(created.id());

		assertThat(job.status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(report.status()).isEqualTo(AnalysisStatus.FAILED);
		assertThat(report.evaluations()).isEmpty();
		assertThat(report.message()).contains("Stockfish is missing");
	}

	@Test
	void runsAnalysisWithInjectedEngine() {
		GameService analyzedGames = new GameService(game -> List.of(
				new MoveEvaluation(1, "e4", "e2e4", 34, "excellent"),
				new MoveEvaluation(2, "e5", "e7e5", -12, "good")),
				Runnable::run);
		var created = analyzedGames.createGame(new CreateGameRequest(
				"1. e4 e5",
				"White",
				"Black",
				"*",
				2,
				null,
				List.of(new MoveRecord(1, "e4", "e5",
						"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
						"rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
						"e2e4",
						"e7e5"))));

		var job = analyzedGames.startAnalysis(created.id());
		var report = analyzedGames.getReport(created.id());

		assertThat(job.status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(report.status()).isEqualTo(AnalysisStatus.READY);
		assertThat(report.message()).contains("2 moves");
		assertThat(report.evaluations()).hasSize(2);
		assertThat(report.evaluations()).extracting("playedMove").containsExactly("e4", "e5");
	}
}
