package com.tanay.chesslab.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.tanay.chesslab.worker.messaging.AnalysisRequest;
import com.tanay.chesslab.worker.messaging.MoveRecord;
import com.tanay.chesslab.worker.stockfish.EngineEvaluation;
import com.tanay.chesslab.worker.stockfish.StockfishClient;

class QueuedGameAnalyzerTest {

	private static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private static final String AFTER_E4_FEN = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

	@Test
	void analyzesQueuedGameMovesWithStockfish() {
		FakeStockfishClient stockfish = new FakeStockfishClient();
		stockfish.add(STARTING_FEN, new EngineEvaluation("e2e4", 34, 6));
		stockfish.add(AFTER_E4_FEN, new EngineEvaluation("c7c5", -12, 6));
		QueuedGameAnalyzer analyzer = new QueuedGameAnalyzer(stockfish);

		List<MoveEvaluation> evaluations = analyzer.analyze(new AnalysisRequest(
				"game-1",
				"job-1",
				6,
				40,
				List.of(new MoveRecord(1, "e4", "e5", STARTING_FEN, AFTER_E4_FEN, "e2e4", "e7e5"))));

		assertThat(evaluations).hasSize(2);
		assertThat(evaluations).extracting("ply").containsExactly(1, 2);
		assertThat(evaluations).extracting("playedMove").containsExactly("e4", "e5");
		assertThat(evaluations).extracting("bestMove").containsExactly("e2e4", "c7c5");
		assertThat(evaluations).extracting("classification").containsExactly("excellent", "inaccuracy");
		assertThat(stockfish.requests).containsExactly(STARTING_FEN, AFTER_E4_FEN);
	}

	@Test
	void respectsRequestedMaxPlies() {
		FakeStockfishClient stockfish = new FakeStockfishClient();
		stockfish.add(STARTING_FEN, new EngineEvaluation("e2e4", 34, 6));
		QueuedGameAnalyzer analyzer = new QueuedGameAnalyzer(stockfish);

		List<MoveEvaluation> evaluations = analyzer.analyze(new AnalysisRequest(
				"game-1",
				"job-1",
				6,
				1,
				List.of(new MoveRecord(1, "e4", "e5", STARTING_FEN, AFTER_E4_FEN, "e2e4", "e7e5"))));

		assertThat(evaluations).hasSize(1);
		assertThat(stockfish.requests).containsExactly(STARTING_FEN);
	}

	@Test
	void rejectsGamesWithoutFenBeforeMoves() {
		QueuedGameAnalyzer analyzer = new QueuedGameAnalyzer(new FakeStockfishClient());

		assertThatThrownBy(() -> analyzer.analyze(new AnalysisRequest(
				"game-1",
				"job-1",
				6,
				40,
				List.of(new MoveRecord(1, "e4", null, null, null, "e2e4", null)))))
				.hasMessageContaining("move FENs are missing");
	}

	private static final class FakeStockfishClient implements StockfishClient {

		private final List<String> requests = new java.util.ArrayList<>();
		private final Map<String, EngineEvaluation> responses = new HashMap<>();

		void add(String fen, EngineEvaluation evaluation) {
			responses.put(fen, evaluation);
		}

		@Override
		public EngineEvaluation analyzeFen(String fen, int depth) {
			requests.add(fen);
			EngineEvaluation evaluation = responses.get(fen);
			if (evaluation == null) {
				throw new IllegalStateException("No fake Stockfish response for " + fen);
			}
			return evaluation;
		}
	}
}
