package com.tanay.chesslab.api.persistence;

import java.time.Instant;
import java.util.List;

import com.tanay.chesslab.api.domain.MoveRecord;

public record NewGame(
		String white,
		String black,
		String result,
		int plyCount,
		Instant createdAt,
		String finalFen,
		List<MoveRecord> moves,
		String pgn) {
}
