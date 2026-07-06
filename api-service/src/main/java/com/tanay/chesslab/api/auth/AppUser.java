package com.tanay.chesslab.api.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "app_users", uniqueConstraints = {
		@UniqueConstraint(name = "uk_app_users_normalized_username", columnNames = "normalized_username")
})
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String username;

	@Column(name = "normalized_username", nullable = false)
	private String normalizedUsername;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private Instant createdAt;

	protected AppUser() {
	}

	AppUser(String username, String normalizedUsername, String passwordHash, Instant createdAt) {
		this.username = username;
		this.normalizedUsername = normalizedUsername;
		this.passwordHash = passwordHash;
		this.createdAt = createdAt;
	}

	public Long id() {
		return id;
	}

	public String username() {
		return username;
	}

	public String normalizedUsername() {
		return normalizedUsername;
	}

	public String passwordHash() {
		return passwordHash;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
