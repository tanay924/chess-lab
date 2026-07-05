package com.tanay.chesslab.api.persistence;

import java.util.List;
import java.util.Optional;

import com.tanay.chesslab.api.domain.AnalysisJob;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.AnalysisStatus;
import com.tanay.chesslab.api.domain.GameDetail;

public interface GameStore {

	GameDetail createGame(NewGame game);

	List<GameDetail> listGames();

	Optional<GameDetail> findGame(String gameId);

	AnalysisJob createJob(String gameId, AnalysisStatus status);

	Optional<AnalysisJob> findCurrentJob(String gameId);

	void updateJobStatus(String jobId, AnalysisStatus status);

	void saveReport(AnalysisReport report);

	Optional<AnalysisReport> findReport(String gameId);
}
