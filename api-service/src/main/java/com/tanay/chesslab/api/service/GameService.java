package com.tanay.chesslab.api.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
	private static final String QUEUED_MESSAGE = "Stockfish analysis is queued. Start the analysis worker with a local Stockfish binary to evaluate moves.";

	private final AtomicLong gameSequence = new AtomicLong(1);
	private final AtomicLong jobSequence = new AtomicLong(1);
	private final Map<String, GameDetail> games = new ConcurrentHashMap<>();
	private final Map<String, AnalysisJob> jobsByGameId = new ConcurrentHashMap<>();
	private final Map<String, AnalysisReport> reportsByGameId = new ConcurrentHashMap<>();

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
		getGame(gameId);
		AnalysisJob job = new AnalysisJob(Long.toString(jobSequence.getAndIncrement()), gameId, AnalysisStatus.QUEUED);
		jobsByGameId.put(gameId, job);
		reportsByGameId.put(gameId,
				new AnalysisReport(gameId, job.id(), AnalysisStatus.QUEUED, QUEUED_MESSAGE, List.of()));
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
}
