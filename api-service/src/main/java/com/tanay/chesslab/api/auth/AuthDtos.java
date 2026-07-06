package com.tanay.chesslab.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record AuthRequest(
			@NotBlank String username,
			@NotBlank @Size(min = 8, message = "Password must be at least 8 characters.") String password) {
	}

	public record SessionResponse(boolean authenticated, String username) {
		static SessionResponse anonymous() {
			return new SessionResponse(false, null);
		}

		static SessionResponse authenticated(AppUser user) {
			return new SessionResponse(true, user.username());
		}
	}

	public record CsrfResponse(String token, String headerName) {
	}
}
