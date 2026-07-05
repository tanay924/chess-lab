package com.tanay.chesslab.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.tanay.chesslab.api.ApiServiceApplication;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.MoveEvaluation;
import com.tanay.chesslab.api.domain.MoveRecord;

class GameServicePersistenceTest {

	@Test
	void gamesAndCompletedReportsSurviveApiRestart() {
		String databaseName = "chess_lab_persistence_" + System.nanoTime();
		String databaseUrl = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1";
		String gameId;

		try (ConfigurableApplicationContext context = applicationContext(databaseUrl)) {
			GameService games = context.getBean(GameService.class);
			var created = games.createGame(sampleGameRequest());
			var job = games.startAnalysis(created.id());

			games.completeAnalysis(new AnalysisReport(
					created.id(),
					job.id(),
					AnalysisStatus.READY,
					"Analyzed 1 moves with Stockfish worker.",
					List.of(new MoveEvaluation(1, "e4", "e2e4", 47, "excellent"))));

			gameId = created.id();
		}

		try (ConfigurableApplicationContext context = applicationContext(databaseUrl)) {
			GameService games = context.getBean(GameService.class);

			var restoredGame = games.getGame(gameId);
			var restoredReport = games.getReport(gameId);

			assertThat(restoredGame.white()).isEqualTo("Ada");
			assertThat(restoredGame.moves()).hasSize(1);
			assertThat(restoredReport.status()).isEqualTo(AnalysisStatus.READY);
			assertThat(restoredReport.evaluations()).hasSize(1);
			assertThat(restoredReport.evaluations().getFirst().bestMove()).isEqualTo("e2e4");
		}
	}

	private static ConfigurableApplicationContext applicationContext(String databaseUrl) {
		return new SpringApplicationBuilder(ApiServiceApplication.class)
				.properties(
						"spring.rabbitmq.listener.simple.auto-startup=false")
				.run(
						"--server.port=0",
						"--spring.datasource.url=" + databaseUrl,
						"--spring.datasource.username=sa",
						"--spring.datasource.password=",
						"--spring.jpa.hibernate.ddl-auto=update");
	}

	private static CreateGameRequest sampleGameRequest() {
		return new CreateGameRequest(
				"[White \"Ada\"]\n[Black \"Grace\"]\n\n1. e4",
				"Ada",
				"Grace",
				"*",
				1,
				"rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
				List.of(new MoveRecord(1, "e4", null,
						"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
						null,
						"e2e4",
						null)));
	}
}
