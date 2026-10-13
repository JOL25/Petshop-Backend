package com.petshop.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.petshop.backend.dto.request.UpdateProfileRequest;
import com.petshop.backend.dto.response.UserProfileUpdateResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.Role;
import com.petshop.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.petshop.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private JwtService jwtService;

	private UserProfileService userProfileService;

	@BeforeEach
	void setUp() {
		userProfileService = new UserProfileService(userRepository, passwordEncoder, userDetailsService, jwtService);
	}

	@Test
	void updatesProfileAndEncodesANewPassword() {
		AppUser user = AppUser.builder()
				.id(4L)
				.name("Cliente anterior")
				.email("cliente@petshop.com")
				.password("old-hash")
				.role(Role.CLIENT)
				.enabled(true)
				.build();
		UpdateProfileRequest request = new UpdateProfileRequest(
				"Cliente actualizado",
				"nuevo@petshop.com",
				"SecurePassword123!"
		);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(request.email())).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("new-hash");
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserDetails userDetails = User.withUsername(request.email()).password("new-hash").roles("CLIENT").build();
		when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
		when(jwtService.generateToken(userDetails)).thenReturn("refreshed-token");

		UserProfileUpdateResponse response = userProfileService.updateMyProfile(user.getEmail(), request);

		assertEquals("Cliente actualizado", response.profile().name());
		assertEquals("nuevo@petshop.com", response.profile().email());
		assertEquals("refreshed-token", response.token());
		assertEquals("new-hash", user.getPassword());
		verify(userRepository).save(user);
	}
}
