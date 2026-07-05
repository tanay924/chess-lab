package com.tanay.chesslab.api.persistence;

import com.tanay.chesslab.api.domain.AnalysisStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_reports")
class JpaAnalysisReportEntity {

	@Id
	private Long gameId;

	@Column(nullable = false)
	private Long jobId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisStatus status;

	@Lob
	@Column(nullable = false)
	private String message;

	@Lob
	@Column(nullable = false)
	private String evaluationsJson;

	protected JpaAnalysisReportEntity() {
	}

	JpaAnalysisReportEntity(Long gameId, Long jobId, AnalysisStatus status, String message, String evaluationsJson) {
		this.gameId = gameId;
		this.jobId = jobId;
		this.status = status;
		this.message = message;
		this.evaluationsJson = evaluationsJson;
	}

	Long gameId() {
		return gameId;
	}

	Long jobId() {
		return jobId;
	}

	AnalysisStatus status() {
		return status;
	}

	String message() {
		return message;
	}

	String evaluationsJson() {
		return evaluationsJson;
	}

	void replaceWith(JpaAnalysisReportEntity report) {
		this.jobId = report.jobId;
		this.status = report.status;
		this.message = report.message;
		this.evaluationsJson = report.evaluationsJson;
	}
}
