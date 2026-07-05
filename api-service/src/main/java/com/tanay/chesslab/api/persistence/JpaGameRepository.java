package com.tanay.chesslab.api.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaGameRepository extends JpaRepository<JpaGameEntity, Long> {

	List<JpaGameEntity> findAllByOrderByCreatedAtDesc();
}
