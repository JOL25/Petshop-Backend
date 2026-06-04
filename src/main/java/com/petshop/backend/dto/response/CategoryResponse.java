package com.petshop.backend.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(
		Long id,
		String name,
		String description,
		Boolean active,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
