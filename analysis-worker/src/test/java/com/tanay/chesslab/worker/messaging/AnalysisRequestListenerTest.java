package com.tanay.chesslab.worker.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tanay.chesslab.worker.analysis.MoveEvaluation;

import tools.jackson.databind.ObjectMapper;

class AnalysisRequestListenerTest {

	@Test
	void publishesReadyResultWhenAnalysisSucceeds() {
		RecordingPublisher publisher = new RecordingPublisher();
		WorkerAnalysisListener listener = new WorkerAnalysisListener(
				request -> List.of(new MoveEvaluation(1, "e4", "e2e4", 34, "excellent")),
				publisher,
				new ObjectMapper());

		listener.handle(new AnalysisRequest("game-1", "job-1", 6, 40, List.of()));

		assertThat(publisher.results).hasSize(2);
		assertThat(publisher.results.getFirst().status()).isEqualTo(AnalysisStatus.RUNNING);
		AnalysisResult result = publisher.results.get(1);
		assertThat(result.gameId()).isEqualTo("game-1");
		assertThat(result.jobId()).isEqualTo("job-1");
		assertThat(result.status()).isEqualTo(AnalysisStatus.READY);
		assertThat(result.message()).contains("1 moves");
		assertThat(result.evaluations()).extracting("playedMove").containsExactly("e4");
	}

	@Test
	void publishesFailedResultWhenAnalysisFails() {
		RecordingPublisher publisher = new RecordingPublisher();
		WorkerAnalysisListener listener = new WorkerAnalysisListener(request -> {
			throw new IllegalStateException("Stockfish is missing");
		}, publisher, new ObjectMapper());

		listener.handle(new AnalysisRequest("game-1", "job-1", 6, 40, List.of()));

		assertThat(publisher.results).hasSize(2);
		assertThat(publisher.results.getFirst().status()).isEqualTo(AnalysisStatus.RUNNING);
		AnalysisResult result = publisher.results.get(1);
		assertThat(result.status()).isEqualTo(AnalysisStatus.FAILED);
		assertThat(result.message()).contains("Stockfish is missing");
		assertThat(result.evaluations()).isEmpty();
	}

	@Test
	void parsesQueuedRequestJsonBeforeAnalyzing() {
		RecordingPublisher publisher = new RecordingPublisher();
		WorkerAnalysisListener listener = new WorkerAnalysisListener(request -> {
			assertThat(request.gameId()).isEqualTo("game-1");
			assertThat(request.jobId()).isEqualTo("job-1");
			assertThat(request.depth()).isEqualTo(6);
			assertThat(request.maxPlies()).isEqualTo(40);
			assertThat(request.moves()).hasSize(1);
			assertThat(request.moves().getFirst().whiteUci()).isEqualTo("e2e4");
			return List.of(new MoveEvaluation(1, "e4", "e2e4", 34, "excellent"));
		}, publisher, new ObjectMapper());

		listener.handleMessage("""
				{
				  "gameId": "game-1",
				  "jobId": "job-1",
				  "depth": 6,
				  "maxPlies": 40,
				  "moves": [
				    {
				      "fullMoveNumber": 1,
				      "whiteMove": "e4",
				      "blackMove": "e5",
				      "whiteFenBefore": "fen-a",
				      "blackFenBefore": "fen-b",
				      "whiteUci": "e2e4",
				      "blackUci": "e7e5"
				    }
				  ]
				}
				""");

		assertThat(publisher.results).hasSize(2);
		assertThat(publisher.results.getFirst().status()).isEqualTo(AnalysisStatus.RUNNING);
		assertThat(publisher.results.get(1).status()).isEqualTo(AnalysisStatus.READY);
	}

	private static final class RecordingPublisher implements AnalysisResultPublisher {

		private final List<AnalysisResult> results = new java.util.ArrayList<>();

		@Override
		public void publish(AnalysisResult result) {
			results.add(result);
		}
	}
}
