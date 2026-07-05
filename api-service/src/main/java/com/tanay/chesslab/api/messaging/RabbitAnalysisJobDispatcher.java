package com.tanay.chesslab.api.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitAnalysisJobDispatcher implements AnalysisJobDispatcher {

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public RabbitAnalysisJobDispatcher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void dispatch(AnalysisRequest request) {
		rabbitTemplate.convertAndSend(
				AnalysisMessaging.EXCHANGE,
				AnalysisMessaging.REQUEST_ROUTING_KEY,
				toJson(request));
	}

	private String toJson(AnalysisRequest request) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (JacksonException error) {
			throw new IllegalStateException("Could not serialize analysis request", error);
		}
	}
}
