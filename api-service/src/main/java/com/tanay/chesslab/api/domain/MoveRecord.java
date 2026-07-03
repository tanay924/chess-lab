package com.tanay.chesslab.api.domain;

public record MoveRecord(
		int fullMoveNumber,
		String whiteMove,
		String blackMove) {
}
