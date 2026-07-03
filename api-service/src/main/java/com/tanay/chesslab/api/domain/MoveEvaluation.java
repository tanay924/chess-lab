package com.tanay.chesslab.api.domain;

public record MoveEvaluation(
		int ply,
		String playedMove,
		String bestMove,
		int scoreCp,
		String classification) {
}
