package com.tanay.chesslab.api.domain;

public record MoveRecord(
		int fullMoveNumber,
		String whiteMove,
		String blackMove,
		String whiteFenBefore,
		String blackFenBefore,
		String whiteUci,
		String blackUci) {
}
