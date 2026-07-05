package com.tanay.chesslab.api.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.tanay.chesslab.api.domain.AnalysisJob;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.GameDetail;

public class InMemoryGameStore implements GameStore {

	private final AtomicLong gameSequence = new AtomicLong(1);
	private final AtomicLong jobSequence = new AtomicLong(1);
	private final Map<String, GameDetail> games = new ConcurrentHashMap<>();
	private final Map<String, AnalysisJob> jobsByGameId = new ConcurrentHashMap<>();
	private final Map<String, AnalysisReport> reportsByGameId = new ConcurrentHashMap<>();

	@Override
	public GameDetail createGame(NewGame game) {
		String id = Long.toString(gameSequence.getAndIncrement());
		GameDetail detail = new GameDetail(
				id,
				game.white(),
				game.black(),
				game.result(),
				game.plyCount(),
				game.createdAt(),
				game.finalFen(),
				game.moves(),
				game.pgn());
		games.put(id, detail);
		return detail;
	}

	@Override
	public List<GameDetail> listGames() {
		return games.values().stream()
				.sorted(Comparator.comparing(GameDetail::createdAt).reversed())
				.toList();
	}

	@Override
	public Optional<GameDetail> findGame(String gameId) {
		return Optional.ofNullable(games.get(gameId));
	}

	@Override
	public AnalysisJob createJob(String gameId, AnalysisStatus status) {
		AnalysisJob job = new AnalysisJob(Long.toString(jobSequence.getAndIncrement()), gameId, status);
		jobsByGameId.put(gameId, job);
		return job;
	}

	@Override
	public Optional<AnalysisJob> findCurrentJob(String gameId) {
		return Optional.ofNullable(jobsByGameId.get(gameId));
	}

	@Override
	public void updateJobStatus(String jobId, AnalysisStatus status) {
		jobsByGameId.computeIfPresent(jobIdFromGame(jobId), (gameId, job) -> new AnalysisJob(job.id(), job.gameId(), status));
	}

	@Override
	public void saveReport(AnalysisReport report) {
		reportsByGameId.put(report.gameId(), report);
	}

	@Override
	public Optional<AnalysisReport> findReport(String gameId) {
		return Optional.ofNullable(reportsByGameId.get(gameId));
	}

	private String jobIdFromGame(String jobId) {
		return jobsByGameId.entrySet().stream()
				.filter(entry -> entry.getValue().id().equals(jobId))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(jobId);
	}
}
