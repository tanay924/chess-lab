package com.tanay.chesslab.api.auth;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tanay.chesslab.api.auth.AuthDtos.AuthRequest;
import com.tanay.chesslab.api.auth.AuthDtos.CsrfResponse;
import com.tanay.chesslab.api.auth.AuthDtos.SessionResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final AuthenticationManager authenticationManager;

	public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
		this.authService = authService;
		this.authenticationManager = authenticationManager;
	}

	@GetMapping("/session")
	public SessionResponse session(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return SessionResponse.anonymous();
		}
		return SessionResponse.authenticated(authService.requireUser(authentication.getName()));
	}

	@GetMapping("/csrf")
	public CsrfResponse csrf(CsrfToken token) {
		return new CsrfResponse(token.getToken(), token.getHeaderName());
	}

	@PostMapping("/register")
	public SessionResponse register(
			@Valid @RequestBody AuthRequest request,
			HttpServletRequest servletRequest) {
		authService.register(request.username(), request.password());
		authenticate(request, servletRequest);
		return session(SecurityContextHolder.getContext().getAuthentication());
	}

	@PostMapping("/login")
	public SessionResponse login(
			@Valid @RequestBody AuthRequest request,
			HttpServletRequest servletRequest) {
		authenticate(request, servletRequest);
		return session(SecurityContextHolder.getContext().getAuthentication());
	}

	@PostMapping("/logout")
	public SessionResponse logout(
			Authentication authentication,
			HttpServletRequest request,
			HttpServletResponse response) {
		new SecurityContextLogoutHandler().logout(request, response, authentication);
		return SessionResponse.anonymous();
	}

	private void authenticate(AuthRequest request, HttpServletRequest servletRequest) {
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					AuthService.normalizeUsername(request.username()),
					request.password()));
		} catch (AuthenticationException error) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
		}
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		servletRequest.getSession(true).setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
	}
}
