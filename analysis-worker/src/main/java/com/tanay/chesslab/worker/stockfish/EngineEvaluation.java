package com.tanay.chesslab.worker.stockfish;

public record EngineEvaluation(
		String bestMove,
		int scoreCp,
		int depth) {
}
