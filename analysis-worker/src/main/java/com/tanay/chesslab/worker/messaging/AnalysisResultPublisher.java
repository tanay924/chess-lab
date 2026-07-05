package com.tanay.chesslab.worker.messaging;

@FunctionalInterface
public interface AnalysisResultPublisher {

	void publish(AnalysisResult result);
}
