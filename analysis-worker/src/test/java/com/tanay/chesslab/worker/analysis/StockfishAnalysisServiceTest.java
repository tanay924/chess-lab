package com.tanay.chesslab.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StockfishAnalysisServiceTest {

	private final StockfishAnalysisService service = new StockfishAnalysisService(new MoveClassifier());

	@Test
	void evaluatesMoveFromEngineScoreDifference() {
		MoveEvaluation evaluation = service.evaluateMove(12, "Nf3", "Bb5", 35, 180);

		assertThat(evaluation.ply()).isEqualTo(12);
		assertThat(evaluation.playedMove()).isEqualTo("Nf3");
		assertThat(evaluation.bestMove()).isEqualTo("Bb5");
		assertThat(evaluation.classification()).isEqualTo("mistake");
	}
}
