package com.tanay.chesslab.worker.messaging;

import java.util.List;

import com.tanay.chesslab.worker.analysis.MoveEvaluation;

public record AnalysisResult(
		String gameId,
		String jobId,
		AnalysisStatus status,
		String message,
		List<MoveEvaluation> evaluations) {
}
