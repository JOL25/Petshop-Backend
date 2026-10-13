package com.petshop.backend.service;

import com.petshop.backend.dto.request.UpdateProfileRequest;
import com.petshop.backend.dto.response.UserProfileResponse;
import com.petshop.backend.dto.response.UserProfileUpdateResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.exception.ResourceNotFoundException;
import com.petshop.backend.repository.UserRepository;
import com.petshop.backend.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;

	public UserProfileService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			UserDetailsService userDetailsService,
			JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getMyProfile(String email) {
		return toResponse(getUserByEmail(email));
	}

	@Transactional
	public UserProfileUpdateResponse updateMyProfile(String currentEmail, UpdateProfileRequest request) {
		AppUser user = getUserByEmail(currentEmail);
		String nextEmail = request.email().trim().toLowerCase();

		if (!user.getEmail().equalsIgnoreCase(nextEmail) && userRepository.existsByEmail(nextEmail)) {
			throw new BadRequestException("Email is already registered");
		}

		user.setName(request.name().trim());
		user.setEmail(nextEmail);
		if (request.password() != null && !request.password().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}

		AppUser savedUser = userRepository.save(user);
		UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
		return new UserProfileUpdateResponse(toResponse(savedUser), jwtService.generateToken(userDetails));
	}

	private AppUser getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private UserProfileResponse toResponse(AppUser user) {
		return new UserProfileResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole().name(),
				user.getCreatedAt()
		);
	}
}
