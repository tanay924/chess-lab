package com.tanay.chesslab.worker.messaging;

import java.util.List;

public record AnalysisRequest(
		String gameId,
		String jobId,
		int depth,
		int maxPlies,
		List<MoveRecord> moves) {
}
