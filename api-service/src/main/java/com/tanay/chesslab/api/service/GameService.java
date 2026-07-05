package com.tanay.chesslab.api.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tanay.chesslab.api.analysis.GameAnalyzer;
import com.tanay.chesslab.api.domain.AnalysisJob;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.GameDetail;
import com.tanay.chesslab.api.domain.GameSummary;
import com.tanay.chesslab.api.domain.MoveRecord;

@Service
public class GameService {

	private static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private static final String RUNNING_MESSAGE = "Stockfish analysis is running locally.";

	private final AtomicLong gameSequence = new AtomicLong(1);
	private final AtomicLong jobSequence = new AtomicLong(1);
	private final GameAnalyzer analyzer;
	private final Executor executor;
	private final ExecutorService ownedExecutor;
	private final Map<String, GameDetail> games = new ConcurrentHashMap<>();
	private final Map<String, AnalysisJob> jobsByGameId = new ConcurrentHashMap<>();
	private final Map<String, AnalysisReport> reportsByGameId = new ConcurrentHashMap<>();

	@Autowired
	public GameService(GameAnalyzer analyzer) {
		this(analyzer, Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "stockfish-analysis");
			thread.setDaemon(true);
			return thread;
		}));
	}

	GameService(GameAnalyzer analyzer, Executor executor) {
		this.analyzer = analyzer;
		this.executor = executor;
		this.ownedExecutor = executor instanceof ExecutorService executorService ? executorService : null;
	}

	@PreDestroy
	public void shutdown() {
		if (ownedExecutor != null) {
			ownedExecutor.shutdownNow();
		}
	}

	public GameDetail createGame(CreateGameRequest request) {
		String id = Long.toString(gameSequence.getAndIncrement());
		List<MoveRecord> moves = sanitizeMoves(request.moves());
		GameDetail detail = new GameDetail(
				id,
				valueOrDefault(request.white(), "White"),
				valueOrDefault(request.black(), "Black"),
				valueOrDefault(request.result(), "*"),
				request.plyCount() == null ? countPlies(moves) : request.plyCount(),
				Instant.now(),
				valueOrDefault(request.finalFen(), STARTING_FEN),
				moves,
				request.pgn());

		games.put(id, detail);
		return detail;
	}

	public List<GameSummary> listGames() {
		return games.values().stream()
				.sorted(Comparator.comparing(GameDetail::createdAt).reversed())
				.map(GameDetail::toSummary)
				.toList();
	}

	public GameDetail getGame(String gameId) {
		GameDetail game = games.get(gameId);
		if (game == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
		}
		return game;
	}

	public AnalysisJob startAnalysis(String gameId) {
		GameDetail game = getGame(gameId);
		AnalysisJob job = new AnalysisJob(Long.toString(jobSequence.getAndIncrement()), gameId, AnalysisStatus.RUNNING);
		jobsByGameId.put(gameId, job);
		reportsByGameId.put(gameId,
				new AnalysisReport(gameId, job.id(), AnalysisStatus.RUNNING, RUNNING_MESSAGE, List.of()));
		executor.execute(() -> runAnalysis(game, job));
		return job;
	}

	public AnalysisReport getReport(String gameId) {
		getGame(gameId);
		AnalysisReport report = reportsByGameId.get(gameId);
		if (report == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis has not been started for this game");
		}
		return report;
	}

	private static List<MoveRecord> sanitizeMoves(List<MoveRecord> moves) {
		if (moves == null) {
			return List.of();
		}
		return moves.stream()
				.filter(Objects::nonNull)
				.filter(move -> move.whiteMove() != null && !move.whiteMove().isBlank())
				.toList();
	}

	private static int countPlies(List<MoveRecord> moves) {
		return moves.stream()
				.mapToInt(move -> move.blackMove() == null || move.blackMove().isBlank() ? 1 : 2)
				.sum();
	}

	private static String valueOrDefault(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private void runAnalysis(GameDetail game, AnalysisJob job) {
		try {
			List<com.tanay.chesslab.api.domain.MoveEvaluation> evaluations = analyzer.analyze(game);
			jobsByGameId.put(game.id(), new AnalysisJob(job.id(), game.id(), AnalysisStatus.READY));
			reportsByGameId.put(game.id(), new AnalysisReport(
					game.id(),
					job.id(),
					AnalysisStatus.READY,
					"Analyzed " + evaluations.size() + " moves with Stockfish.",
					evaluations));
		} catch (Exception error) {
			jobsByGameId.put(game.id(), new AnalysisJob(job.id(), game.id(), AnalysisStatus.FAILED));
			reportsByGameId.put(game.id(), new AnalysisReport(
					game.id(),
					job.id(),
					AnalysisStatus.FAILED,
					error.getMessage() == null ? "Stockfish analysis failed." : error.getMessage(),
					List.of()));
		}
	}
}
