package com.petshop.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.petshop.backend.dto.request.LoginRequest;
import com.petshop.backend.dto.response.AuthResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.Role;
import com.petshop.backend.repository.UserRepository;
import com.petshop.backend.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(
				authenticationManager,
				userDetailsService,
				userRepository,
				passwordEncoder,
				jwtService
		);
	}

	@Test
	void loginAuthenticatesCredentialsAndReturnsJwt() {
		LoginRequest request = new LoginRequest("admin@petshop.com", "SecurePassword123!");
		AppUser user = AppUser.builder()
				.id(1L)
				.name("Administrador")
				.email(request.email())
				.password("bcrypt-hash")
				.role(Role.ADMIN)
				.enabled(true)
				.build();
		UserDetails userDetails = User.withUsername(request.email())
				.password(user.getPassword())
				.roles("ADMIN")
				.build();

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
		when(jwtService.generateToken(userDetails)).thenReturn("signed-jwt");

		AuthResponse response = authService.login(request);

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		assertEquals("signed-jwt", response.token());
		assertEquals("Bearer", response.tokenType());
		assertEquals("ADMIN", response.role());
		assertEquals(request.email(), response.email());
	}
}
