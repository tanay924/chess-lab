package com.tanay.chesslab.api.domain;

import java.time.Instant;
import java.util.List;

public record GameDetail(
		String id,
		String white,
		String black,
		String result,
		int plyCount,
		Instant createdAt,
		String finalFen,
		List<MoveRecord> moves,
		String pgn) {

	public GameSummary toSummary() {
		return new GameSummary(id, white, black, result, plyCount, createdAt);
	}
}
