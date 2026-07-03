package com.tanay.chesslab.worker.analysis;

import org.springframework.stereotype.Component;

@Component
public class MoveClassifier {

	public MoveClassification classify(int centipawnLoss) {
		int loss = Math.max(0, centipawnLoss);
		if (loss <= 20) {
			return MoveClassification.EXCELLENT;
		}
		if (loss <= 60) {
			return MoveClassification.GOOD;
		}
		if (loss <= 120) {
			return MoveClassification.INACCURACY;
		}
		if (loss <= 300) {
			return MoveClassification.MISTAKE;
		}
		return MoveClassification.BLUNDER;
	}
}
