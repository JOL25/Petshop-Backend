package com.petshop.backend.service;

import com.petshop.backend.dto.request.LoginRequest;
import com.petshop.backend.dto.request.RegisterAdminRequest;
import com.petshop.backend.dto.response.AuthResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.Role;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.repository.UserRepository;
import com.petshop.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			AuthenticationManager authenticationManager,
			UserDetailsService userDetailsService,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		AppUser user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BadRequestException("Invalid credentials"));
		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
		String token = jwtService.generateToken(userDetails);

		return toAuthResponse(token, user);
	}

	@Transactional
	public AuthResponse registerAdmin(RegisterAdminRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BadRequestException("Email is already registered");
		}

		AppUser user = AppUser.builder()
				.name(request.name())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.role(Role.ADMIN)
				.enabled(true)
				.build();

		AppUser savedUser = userRepository.save(user);
		UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
		String token = jwtService.generateToken(userDetails);

		return toAuthResponse(token, savedUser);
	}

	private AuthResponse toAuthResponse(String token, AppUser user) {
		return new AuthResponse(
				token,
				TOKEN_TYPE,
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole().name()
		);
	}
}
