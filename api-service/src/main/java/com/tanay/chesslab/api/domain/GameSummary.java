package com.tanay.chesslab.api.domain;

import java.time.Instant;

public record GameSummary(
		String id,
		String white,
		String black,
		String result,
		int plyCount,
		Instant createdAt) {
}
