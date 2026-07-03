package com.tanay.chesslab.api.domain;

import java.util.List;

public record AnalysisReport(
		String gameId,
		String jobId,
		AnalysisStatus status,
		String message,
		List<MoveEvaluation> evaluations) {
}
