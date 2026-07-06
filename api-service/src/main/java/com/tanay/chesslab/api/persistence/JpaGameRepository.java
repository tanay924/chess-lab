package com.tanay.chesslab.api.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaGameRepository extends JpaRepository<JpaGameEntity, Long> {

	List<JpaGameEntity> findAllByOrderByCreatedAtDesc();

	List<JpaGameEntity> findAllByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

	Optional<JpaGameEntity> findByIdAndOwnerUsername(Long id, String ownerUsername);
}
