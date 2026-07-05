package com.tanay.chesslab.worker.analysis;

import org.springframework.stereotype.Service;

@Service
public class StockfishAnalysisService {

	private final MoveClassifier classifier;

	public StockfishAnalysisService(MoveClassifier classifier) {
		this.classifier = classifier;
	}

	public MoveEvaluation evaluateMove(int ply, String playedMove, String bestMove, int playedScoreCp, int bestScoreCp) {
		int centipawnLoss = Math.abs(bestScoreCp - playedScoreCp);
		return new MoveEvaluation(ply, playedMove, bestMove, playedScoreCp,
				classifier.classify(centipawnLoss).name().toLowerCase());
	}
}
