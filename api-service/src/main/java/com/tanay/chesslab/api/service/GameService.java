package com.tanay.chesslab.api.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.tanay.chesslab.api.messaging.AnalysisJobDispatcher;
import com.tanay.chesslab.api.messaging.AnalysisRequest;
import com.tanay.chesslab.api.persistence.GameStore;
import com.tanay.chesslab.api.persistence.InMemoryGameStore;
import com.tanay.chesslab.api.persistence.NewGame;

@Service
public class GameService {

	private static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private static final String RUNNING_MESSAGE = "Stockfish analysis has been queued for the worker.";

	private final AnalysisJobDispatcher dispatcher;
	private final GameStore store;
	private final int depth;
	private final int maxPlies;

	@Autowired
	public GameService(
			AnalysisJobDispatcher dispatcher,
			GameStore store,
			@Value("${analysis.stockfish.depth:8}") int depth,
			@Value("${analysis.max-plies:80}") int maxPlies) {
		this.dispatcher = dispatcher;
		this.store = store;
		this.depth = Math.max(1, depth);
		this.maxPlies = Math.max(1, maxPlies);
	}

	public GameService(AnalysisJobDispatcher dispatcher, int depth, int maxPlies) {
		this(dispatcher, new InMemoryGameStore(), depth, maxPlies);
	}

	public GameDetail createGame(CreateGameRequest request) {
		List<MoveRecord> moves = sanitizeMoves(request.moves());
		return store.createGame(new NewGame(
				valueOrDefault(request.white(), "White"),
				valueOrDefault(request.black(), "Black"),
				valueOrDefault(request.result(), "*"),
				request.plyCount() == null ? countPlies(moves) : request.plyCount(),
				Instant.now(),
				valueOrDefault(request.finalFen(), STARTING_FEN),
				moves,
				valueOrDefault(request.pgn(), "")));
	}

	public List<GameSummary> listGames() {
		return store.listGames().stream()
				.map(GameDetail::toSummary)
				.toList();
	}

	public GameDetail getGame(String gameId) {
		return store.findGame(gameId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
	}

	public AnalysisJob startAnalysis(String gameId) {
		GameDetail game = getGame(gameId);
		AnalysisJob job = store.createJob(gameId, AnalysisStatus.RUNNING);
		store.saveReport(new AnalysisReport(gameId, job.id(), AnalysisStatus.RUNNING, RUNNING_MESSAGE, List.of()));
		try {
			dispatcher.dispatch(new AnalysisRequest(game.id(), job.id(), depth, maxPlies, game.moves()));
		} catch (Exception error) {
			job = new AnalysisJob(job.id(), gameId, AnalysisStatus.FAILED);
			store.updateJobStatus(job.id(), AnalysisStatus.FAILED);
			store.saveReport(new AnalysisReport(
					gameId,
					job.id(),
					AnalysisStatus.FAILED,
					error.getMessage() == null ? "Could not queue Stockfish analysis." : error.getMessage(),
					List.of()));
		}
		return job;
	}

	public AnalysisReport getReport(String gameId) {
		getGame(gameId);
		return store.findReport(gameId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Analysis has not been started for this game"));
	}

	public void completeAnalysis(AnalysisReport report) {
		if (report == null) {
			return;
		}
		AnalysisJob currentJob = store.findCurrentJob(report.gameId()).orElse(null);
		if (currentJob == null || !currentJob.id().equals(report.jobId())) {
			return;
		}
		store.updateJobStatus(report.jobId(), report.status());
		store.saveReport(report);
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
