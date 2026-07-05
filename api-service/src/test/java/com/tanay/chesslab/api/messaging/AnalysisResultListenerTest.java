package com.tanay.chesslab.api.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.MoveRecord;
import com.tanay.chesslab.api.service.GameService;

import tools.jackson.databind.ObjectMapper;

class AnalysisResultListenerTest {

	@Test
	void parsesWorkerResultJsonAndCompletesReport() {
		GameService games = new GameService(request -> {
		}, 6, 40);
		var created = games.createGame(new CreateGameRequest(
				"1. e4 e5",
				"White",
				"Black",
				"*",
				2,
				null,
				List.of(new MoveRecord(1, "e4", "e5", "fen-a", "fen-b", "e2e4", "e7e5"))));
		var job = games.startAnalysis(created.id());
		AnalysisResultListener listener = new AnalysisResultListener(games, new ObjectMapper());

		listener.handle("""
				{
				  "gameId": "%s",
				  "jobId": "%s",
				  "status": "ready",
				  "message": "Analyzed 1 moves with Stockfish worker.",
				  "evaluations": [
				    {
				      "ply": 1,
				      "playedMove": "e4",
				      "bestMove": "e2e4",
				      "scoreCp": 34,
				      "classification": "excellent"
				    }
				  ]
				}
				""".formatted(created.id(), job.id()));

		var report = games.getReport(created.id());

		assertThat(report.status()).isEqualTo(AnalysisStatus.READY);
		assertThat(report.evaluations()).hasSize(1);
		assertThat(report.evaluations().getFirst().bestMove()).isEqualTo("e2e4");
	}
}
