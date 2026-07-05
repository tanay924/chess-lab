package com.tanay.chesslab.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.MoveEvaluation;
import com.tanay.chesslab.api.domain.MoveRecord;
import com.tanay.chesslab.api.messaging.AnalysisJobDispatcher;
import com.tanay.chesslab.api.messaging.AnalysisRequest;

class GameServiceTest {

	private final RecordingDispatcher dispatcher = new RecordingDispatcher();
	private final GameService games = new GameService(dispatcher, 6, 40);

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
	void publishesAnalysisRequestToWorkerQueue() {
		var created = games.createGame(sampleGameRequest());

		var job = games.startAnalysis(created.id());
		var report = games.getReport(created.id());

		assertThat(job.status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(report.status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(report.message()).contains("queued");
		assertThat(dispatcher.requests).hasSize(1);
		AnalysisRequest request = dispatcher.requests.getFirst();
		assertThat(request.gameId()).isEqualTo(created.id());
		assertThat(request.jobId()).isEqualTo(job.id());
		assertThat(request.depth()).isEqualTo(6);
		assertThat(request.maxPlies()).isEqualTo(40);
		assertThat(request.moves()).hasSize(1);
	}

	@Test
	void marksAnalysisFailedWhenQueuePublishFails() {
		GameService failingGames = new GameService(request -> {
			throw new IllegalStateException("RabbitMQ is missing");
		}, 6, 40);
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

		assertThat(job.status()).isEqualTo(AnalysisStatus.FAILED);
		assertThat(report.status()).isEqualTo(AnalysisStatus.FAILED);
		assertThat(report.evaluations()).isEmpty();
		assertThat(report.message()).contains("RabbitMQ is missing");
	}

	@Test
	void acceptsReadyAnalysisResultFromWorker() {
		var created = games.createGame(sampleGameRequest());
		var job = games.startAnalysis(created.id());

		games.completeAnalysis(new AnalysisReport(
				created.id(),
				job.id(),
				AnalysisStatus.READY,
				"Analyzed 2 moves with Stockfish worker.",
				List.of(
						new MoveEvaluation(1, "e4", "e2e4", 34, "excellent"),
						new MoveEvaluation(2, "e5", "e7e5", -12, "good"))));
		var report = games.getReport(created.id());

		assertThat(report.status()).isEqualTo(AnalysisStatus.READY);
		assertThat(report.message()).contains("2 moves");
		assertThat(report.evaluations()).hasSize(2);
		assertThat(report.evaluations()).extracting("playedMove").containsExactly("e4", "e5");
	}

	@Test
	void ignoresWorkerResultForSupersededJob() {
		var created = games.createGame(sampleGameRequest());
		var firstJob = games.startAnalysis(created.id());
		var secondJob = games.startAnalysis(created.id());

		games.completeAnalysis(new AnalysisReport(
				created.id(),
				firstJob.id(),
				AnalysisStatus.READY,
				"Old result",
				List.of(new MoveEvaluation(1, "e4", "e2e4", 34, "excellent"))));

		var report = games.getReport(created.id());

		assertThat(secondJob.status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(report.status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(report.message()).doesNotContain("Old result");
	}

	private static CreateGameRequest sampleGameRequest() {
		return new CreateGameRequest(
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
	}

	private static final class RecordingDispatcher implements AnalysisJobDispatcher {

		private final List<AnalysisRequest> requests = new ArrayList<>();

		@Override
		public void dispatch(AnalysisRequest request) {
			requests.add(request);
		}
	}
}
