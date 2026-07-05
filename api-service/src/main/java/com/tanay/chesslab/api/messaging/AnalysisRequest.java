package com.tanay.chesslab.api.messaging;

import java.util.List;

import com.tanay.chesslab.api.domain.MoveRecord;

public record AnalysisRequest(
		String gameId,
		String jobId,
		int depth,
		int maxPlies,
		List<MoveRecord> moves) {
}
