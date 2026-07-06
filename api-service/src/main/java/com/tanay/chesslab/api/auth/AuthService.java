package com.tanay.chesslab.api.auth;

import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final AppUserRepository users;
	private final PasswordEncoder passwordEncoder;

	public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AppUser register(String username, String password) {
		String displayUsername = cleanUsername(username);
		String normalizedUsername = normalizeUsername(displayUsername);
		if (displayUsername.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
		}
		if (password == null || password.length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters.");
		}
		if (users.existsByNormalizedUsername(normalizedUsername)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
		}
		return users.save(new AppUser(
				displayUsername,
				normalizedUsername,
				passwordEncoder.encode(password),
				Instant.now()));
	}

	@Transactional(readOnly = true)
	public AppUser requireUser(String normalizedUsername) {
		return users.findByNormalizedUsername(normalizeUsername(normalizedUsername))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));
	}

	public static String normalizeUsername(String username) {
		return cleanUsername(username).toLowerCase(Locale.ROOT);
	}

	private static String cleanUsername(String username) {
		return username == null ? "" : username.trim();
	}
}
