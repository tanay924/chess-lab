package com.tanay.chesslab.api.analysis;

public record EngineEvaluation(
		String bestMove,
		int scoreCp,
		int depth) {
}
