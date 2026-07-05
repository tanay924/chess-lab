package com.tanay.chesslab.worker.messaging;

public final class AnalysisMessaging {

	public static final String EXCHANGE = "chesslab.analysis";
	public static final String REQUEST_QUEUE = "chesslab.analysis.requests";
	public static final String RESULT_QUEUE = "chesslab.analysis.results";
	public static final String REQUEST_ROUTING_KEY = "analysis.request";
	public static final String RESULT_ROUTING_KEY = "analysis.result";

	private AnalysisMessaging() {
	}
}
