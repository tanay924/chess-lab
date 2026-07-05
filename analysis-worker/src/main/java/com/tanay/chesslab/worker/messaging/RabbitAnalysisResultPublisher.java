package com.tanay.chesslab.worker.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitAnalysisResultPublisher implements AnalysisResultPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public RabbitAnalysisResultPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void publish(AnalysisResult result) {
		rabbitTemplate.convertAndSend(
				AnalysisMessaging.EXCHANGE,
				AnalysisMessaging.RESULT_ROUTING_KEY,
				toJson(result));
	}

	private String toJson(AnalysisResult result) {
		try {
			return objectMapper.writeValueAsString(result);
		} catch (JacksonException error) {
			throw new IllegalStateException("Could not serialize analysis result", error);
		}
	}
}
