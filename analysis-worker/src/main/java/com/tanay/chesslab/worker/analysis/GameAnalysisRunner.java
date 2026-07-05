package com.tanay.chesslab.worker.analysis;

import java.util.List;

import com.tanay.chesslab.worker.messaging.AnalysisRequest;

@FunctionalInterface
public interface GameAnalysisRunner {

	List<MoveEvaluation> analyze(AnalysisRequest request);
}
