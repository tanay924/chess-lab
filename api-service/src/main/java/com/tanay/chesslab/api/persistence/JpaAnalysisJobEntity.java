package com.tanay.chesslab.api.persistence;

import java.time.Instant;

import com.tanay.chesslab.api.domain.AnalysisStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_jobs")
class JpaAnalysisJobEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long gameId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisStatus status;

	@Column(nullable = false)
	private Instant createdAt;

	protected JpaAnalysisJobEntity() {
	}

	JpaAnalysisJobEntity(Long gameId, AnalysisStatus status, Instant createdAt) {
		this.gameId = gameId;
		this.status = status;
		this.createdAt = createdAt;
	}

	Long id() {
		return id;
	}

	Long gameId() {
		return gameId;
	}

	AnalysisStatus status() {
		return status;
	}

	void status(AnalysisStatus status) {
		this.status = status;
	}
}
