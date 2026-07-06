package com.tanay.chesslab.api.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByNormalizedUsername(String normalizedUsername);

	boolean existsByNormalizedUsername(String normalizedUsername);
}
