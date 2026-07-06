package com.tanay.chesslab.worker.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.tanay.chesslab.worker.analysis.GameAnalysisRunner;
import com.tanay.chesslab.worker.analysis.MoveEvaluation;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class WorkerAnalysisListener {

	private final GameAnalysisRunner analyzer;
	private final AnalysisResultPublisher publisher;
	private final ObjectMapper objectMapper;

	public WorkerAnalysisListener(
			GameAnalysisRunner analyzer,
			AnalysisResultPublisher publisher,
			ObjectMapper objectMapper) {
		this.analyzer = analyzer;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(queues = AnalysisMessaging.REQUEST_QUEUE)
	public void handleMessage(String payload) {
		handle(parse(payload));
	}

	public void handle(AnalysisRequest request) {
		try {
			publisher.publish(new AnalysisResult(
					request.gameId(),
					request.jobId(),
					AnalysisStatus.RUNNING,
					"Stockfish analysis is running locally.",
					List.of()));
			List<MoveEvaluation> evaluations = analyzer.analyze(request);
			publisher.publish(new AnalysisResult(
					request.gameId(),
					request.jobId(),
					AnalysisStatus.READY,
					"Analyzed " + evaluations.size() + " moves with Stockfish worker.",
					evaluations));
		} catch (Exception error) {
			publisher.publish(new AnalysisResult(
					request.gameId(),
					request.jobId(),
					AnalysisStatus.FAILED,
					error.getMessage() == null ? "Stockfish analysis failed." : error.getMessage(),
					List.of()));
		}
	}

	private AnalysisRequest parse(String payload) {
		try {
			return objectMapper.readValue(payload, AnalysisRequest.class);
		} catch (JacksonException error) {
			throw new IllegalArgumentException("Could not parse analysis request", error);
		}
	}
}
