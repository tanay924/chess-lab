package com.tanay.chesslab.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MoveClassifierTest {

	private final MoveClassifier classifier = new MoveClassifier();

	@Test
	void classifiesCentipawnLossBuckets() {
		assertThat(classifier.classify(0)).isEqualTo(MoveClassification.EXCELLENT);
		assertThat(classifier.classify(55)).isEqualTo(MoveClassification.GOOD);
		assertThat(classifier.classify(100)).isEqualTo(MoveClassification.INACCURACY);
		assertThat(classifier.classify(250)).isEqualTo(MoveClassification.MISTAKE);
		assertThat(classifier.classify(450)).isEqualTo(MoveClassification.BLUNDER);
	}
}
