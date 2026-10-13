package com.petshop.backend.controller;

import com.petshop.backend.dto.request.UpdateProfileRequest;
import com.petshop.backend.dto.response.UserProfileResponse;
import com.petshop.backend.dto.response.UserProfileUpdateResponse;
import com.petshop.backend.service.UserProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Perfil", description = "Perfil del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

	private final UserProfileService userProfileService;

	public UserProfileController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping
	public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
		return ResponseEntity.ok(userProfileService.getMyProfile(principal.getName()));
	}

	@PutMapping
	public ResponseEntity<UserProfileUpdateResponse> updateMyProfile(
			Principal principal,
			@Valid @RequestBody UpdateProfileRequest request
	) {
		return ResponseEntity.ok(userProfileService.updateMyProfile(principal.getName(), request));
	}
}
