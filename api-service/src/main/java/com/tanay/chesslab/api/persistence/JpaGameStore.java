package com.tanay.chesslab.api.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tanay.chesslab.api.domain.AnalysisJob;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.GameDetail;
import com.tanay.chesslab.api.domain.MoveEvaluation;
import com.tanay.chesslab.api.domain.MoveRecord;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
class JpaGameStore implements GameStore {

	private static final TypeReference<List<MoveRecord>> MOVES_TYPE = new TypeReference<>() {
	};
	private static final TypeReference<List<MoveEvaluation>> EVALUATIONS_TYPE = new TypeReference<>() {
	};

	private final JpaGameRepository games;
	private final JpaAnalysisJobRepository jobs;
	private final JpaAnalysisReportRepository reports;
	private final ObjectMapper objectMapper;

	JpaGameStore(
			JpaGameRepository games,
			JpaAnalysisJobRepository jobs,
			JpaAnalysisReportRepository reports,
			ObjectMapper objectMapper) {
		this.games = games;
		this.jobs = jobs;
		this.reports = reports;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public GameDetail createGame(NewGame game) {
		JpaGameEntity entity = games.save(new JpaGameEntity(game, write(game.moves())));
		return toDetail(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<GameDetail> listGames() {
		return games.findAllByOrderByCreatedAtDesc().stream()
				.map(this::toDetail)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<GameDetail> findGame(String gameId) {
		return parseId(gameId).flatMap(games::findById).map(this::toDetail);
	}

	@Override
	@Transactional
	public AnalysisJob createJob(String gameId, AnalysisStatus status) {
		Long parsedGameId = parseRequiredId(gameId, "game");
		JpaAnalysisJobEntity entity = jobs.save(new JpaAnalysisJobEntity(parsedGameId, status, Instant.now()));
		return toJob(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AnalysisJob> findCurrentJob(String gameId) {
		return parseId(gameId).flatMap(jobs::findFirstByGameIdOrderByIdDesc).map(this::toJob);
	}

	@Override
	@Transactional
	public void updateJobStatus(String jobId, AnalysisStatus status) {
		Long parsedJobId = parseRequiredId(jobId, "job");
		JpaAnalysisJobEntity entity = jobs.findById(parsedJobId)
				.orElseThrow(() -> new IllegalArgumentException("Analysis job not found: " + jobId));
		entity.status(status);
	}

	@Override
	@Transactional
	public void saveReport(AnalysisReport report) {
		Long gameId = parseRequiredId(report.gameId(), "game");
		Long jobId = parseRequiredId(report.jobId(), "job");
		JpaAnalysisReportEntity incoming = new JpaAnalysisReportEntity(
				gameId,
				jobId,
				report.status(),
				report.message() == null ? "" : report.message(),
				write(report.evaluations() == null ? List.of() : report.evaluations()));
		reports.findById(gameId)
				.ifPresentOrElse(existing -> existing.replaceWith(incoming), () -> reports.save(incoming));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AnalysisReport> findReport(String gameId) {
		return parseId(gameId).flatMap(reports::findById).map(this::toReport);
	}

	private GameDetail toDetail(JpaGameEntity entity) {
		return new GameDetail(
				entity.id().toString(),
				entity.whiteName(),
				entity.blackName(),
				entity.result(),
				entity.plyCount(),
				entity.createdAt(),
				entity.finalFen(),
				read(entity.movesJson(), MOVES_TYPE),
				entity.pgn());
	}

	private AnalysisJob toJob(JpaAnalysisJobEntity entity) {
		return new AnalysisJob(entity.id().toString(), entity.gameId().toString(), entity.status());
	}

	private AnalysisReport toReport(JpaAnalysisReportEntity entity) {
		return new AnalysisReport(
				entity.gameId().toString(),
				entity.jobId().toString(),
				entity.status(),
				entity.message(),
				read(entity.evaluationsJson(), EVALUATIONS_TYPE));
	}

	private Optional<Long> parseId(String id) {
		try {
			return Optional.of(Long.parseLong(id));
		} catch (NumberFormatException error) {
			return Optional.empty();
		}
	}

	private Long parseRequiredId(String id, String label) {
		return parseId(id).orElseThrow(() -> new IllegalArgumentException("Invalid " + label + " id: " + id));
	}

	private String write(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JacksonException error) {
			throw new IllegalStateException("Could not serialize stored chess data", error);
		}
	}

	private <T> T read(String value, TypeReference<T> type) {
		try {
			return objectMapper.readValue(value, type);
		} catch (JacksonException error) {
			throw new IllegalStateException("Could not parse stored chess data", error);
		}
	}
}
