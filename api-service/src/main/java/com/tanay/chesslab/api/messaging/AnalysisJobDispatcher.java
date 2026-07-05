package com.tanay.chesslab.api.messaging;

@FunctionalInterface
public interface AnalysisJobDispatcher {

	void dispatch(AnalysisRequest request);
}
