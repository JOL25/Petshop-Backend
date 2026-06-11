package com.petshop.backend.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.petshop.backend.security.JwtAuthenticationFilter;
import java.util.List;
import org.junit.jupiter.api.Test;

class SecurityConfigTests {

	@Test
	void rejectsWildcardCorsOrigin() {
		JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);

		assertThrows(
				IllegalArgumentException.class,
				() -> new SecurityConfig(filter, List.of("*"))
		);
	}
}
