package com.tanay.chesslab.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.MoveRecord;

class GameServiceTest {

	private final GameService games = new GameService();

	@Test
	void createsAndListsImportedGame() {
		CreateGameRequest request = new CreateGameRequest(
				"[White \"Ada\"]\n[Black \"Grace\"]\n\n1. e4 e5",
				"Ada",
				"Grace",
				"*",
				2,
				"rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
				List.of(new MoveRecord(1, "e4", "e5")));

		var created = games.createGame(request);

		assertThat(created.id()).isEqualTo("1");
		assertThat(created.white()).isEqualTo("Ada");
		assertThat(created.black()).isEqualTo("Grace");
		assertThat(created.moves()).hasSize(1);
		assertThat(games.listGames()).extracting("id").containsExactly("1");
	}

	@Test
	void startsQueuedAnalysisWithoutInventingEngineResults() {
		var created = games.createGame(new CreateGameRequest(
				"1. e4 e5",
				"White",
				"Black",
				"*",
				2,
				null,
				List.of(new MoveRecord(1, "e4", "e5"))));

		var job = games.startAnalysis(created.id());
		var report = games.getReport(created.id());

		assertThat(job.status()).isEqualTo(AnalysisStatus.QUEUED);
		assertThat(report.status()).isEqualTo(AnalysisStatus.QUEUED);
		assertThat(report.evaluations()).isEmpty();
		assertThat(report.message()).contains("Stockfish");
	}
}
