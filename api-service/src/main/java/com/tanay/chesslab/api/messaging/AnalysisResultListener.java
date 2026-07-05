package com.tanay.chesslab.api.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.service.GameService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class AnalysisResultListener {

	private final GameService games;
	private final ObjectMapper objectMapper;

	public AnalysisResultListener(GameService games, ObjectMapper objectMapper) {
		this.games = games;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(queues = AnalysisMessaging.RESULT_QUEUE)
	public void handle(String payload) {
		games.completeAnalysis(parse(payload));
	}

	private AnalysisReport parse(String payload) {
		try {
			return objectMapper.readValue(payload, AnalysisReport.class);
		} catch (JacksonException error) {
			throw new IllegalArgumentException("Could not parse analysis result", error);
		}
	}
}
