package com.tanay.chesslab.api.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaAnalysisJobRepository extends JpaRepository<JpaAnalysisJobEntity, Long> {

	Optional<JpaAnalysisJobEntity> findFirstByGameIdOrderByIdDesc(Long gameId);
}
