package com.tanay.chesslab.api.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisRabbitConfig {

	@Bean
	DirectExchange analysisExchange() {
		return new DirectExchange(AnalysisMessaging.EXCHANGE, true, false);
	}

	@Bean
	Queue analysisRequestQueue() {
		return new Queue(AnalysisMessaging.REQUEST_QUEUE, true);
	}

	@Bean
	Queue analysisResultQueue() {
		return new Queue(AnalysisMessaging.RESULT_QUEUE, true);
	}

	@Bean
	Binding analysisRequestBinding(Queue analysisRequestQueue, DirectExchange analysisExchange) {
		return BindingBuilder.bind(analysisRequestQueue)
				.to(analysisExchange)
				.with(AnalysisMessaging.REQUEST_ROUTING_KEY);
	}

	@Bean
	Binding analysisResultBinding(Queue analysisResultQueue, DirectExchange analysisExchange) {
		return BindingBuilder.bind(analysisResultQueue)
				.to(analysisExchange)
				.with(AnalysisMessaging.RESULT_ROUTING_KEY);
	}

}
