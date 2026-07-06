package com.tanay.chesslab.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {

	private final AppUserRepository users = mock(AppUserRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final AuthService auth = new AuthService(users, passwordEncoder);

	@Test
	void registersCaseInsensitiveUsernameAndHashesPassword() {
		when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AppUser registered = auth.register(" Tanay ", "password123");

		assertThat(registered.username()).isEqualTo("Tanay");
		assertThat(registered.normalizedUsername()).isEqualTo("tanay");
		assertThat(registered.passwordHash()).isNotEqualTo("password123");
		assertThat(passwordEncoder.matches("password123", registered.passwordHash())).isTrue();
		ArgumentCaptor<AppUser> savedUser = ArgumentCaptor.forClass(AppUser.class);
		verify(users).save(savedUser.capture());
		assertThat(savedUser.getValue().normalizedUsername()).isEqualTo("tanay");
	}

	@Test
	void rejectsDuplicateUsernameIgnoringCase() {
		when(users.existsByNormalizedUsername("tanay")).thenReturn(true);

		assertThatThrownBy(() -> auth.register("TANAY", "password123"))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(error -> ((ResponseStatusException) error).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void rejectsShortPassword() {
		assertThatThrownBy(() -> auth.register("tanay", "short"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Password must be at least 8 characters");
	}
}
