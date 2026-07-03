package com.tanay.chesslab.worker.analysis;

public record MoveEvaluation(
		int ply,
		String playedMove,
		String bestMove,
		int scoreCp,
		MoveClassification classification) {
}
