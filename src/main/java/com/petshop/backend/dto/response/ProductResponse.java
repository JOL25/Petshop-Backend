package com.petshop.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
		Long id,
		String name,
		String description,
		BigDecimal price,
		Integer stock,
		String imageUrl,
		Boolean active,
		Long categoryId,
		String categoryName,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
