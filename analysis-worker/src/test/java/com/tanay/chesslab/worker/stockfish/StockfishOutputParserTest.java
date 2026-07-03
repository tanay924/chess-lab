package com.tanay.chesslab.worker.stockfish;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StockfishOutputParserTest {

	@Test
	void parsesBestMoveAndCentipawnScore() {
		StockfishOutputParser parser = new StockfishOutputParser();

		parser.accept("info depth 14 seldepth 22 score cp 37 nodes 1000 pv e2e4 e7e5");
		parser.accept("bestmove e2e4 ponder e7e5");

		EngineEvaluation evaluation = parser.toEvaluation(14);

		assertThat(evaluation.bestMove()).isEqualTo("e2e4");
		assertThat(evaluation.scoreCp()).isEqualTo(37);
		assertThat(evaluation.depth()).isEqualTo(14);
	}

	@Test
	void convertsMateScoreToLargeCentipawnValue() {
		StockfishOutputParser parser = new StockfishOutputParser();

		parser.accept("info depth 20 score mate -3 pv h2h4");
		parser.accept("bestmove h2h4");

		assertThat(parser.toEvaluation(20).scoreCp()).isEqualTo(-100_000);
	}
}
