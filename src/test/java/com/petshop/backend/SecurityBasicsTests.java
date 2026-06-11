package com.petshop.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.petshop.backend.security.JwtService;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityBasicsTests {

	private static final String SECURE_JWT_SECRET =
			"petshop_test_secret_with_at_least_32_characters";

	@Test
	void bcryptEncodesPasswordsWithoutStoringPlainText() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		String plainPassword = "SecurePassword123!";
		String encodedPassword = encoder.encode(plainPassword);

		assertFalse(plainPassword.equals(encodedPassword));
		assertTrue(encodedPassword.startsWith("$2"));
		assertTrue(encoder.matches(plainPassword, encodedPassword));
	}

	@Test
	void jwtContainsExpirationAndValidatesItsOwner() {
		JwtService jwtService = new JwtService(SECURE_JWT_SECRET, 60_000);
		UserDetails user = User.withUsername("admin@petshop.com")
				.password("ignored")
				.roles("ADMIN")
				.build();

		String token = jwtService.generateToken(user);

		assertTrue(jwtService.isTokenValid(token, user));
	}

	@Test
	void jwtRejectsInvalidExpiration() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new JwtService(SECURE_JWT_SECRET, 0)
		);
	}

	@Test
	void jwtRejectsWeakSecrets() {
		assertThrows(
				WeakKeyException.class,
				() -> new JwtService("short-secret", 60_000)
		);
	}
}
