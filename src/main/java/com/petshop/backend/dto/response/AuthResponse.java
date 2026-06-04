package com.petshop.backend.dto.response;

public record AuthResponse(
		String token,
		String tokenType,
		Long userId,
		String name,
		String email,
		String role
) {
}
