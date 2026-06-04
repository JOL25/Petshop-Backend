package com.petshop.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
		@NotBlank(message = "Name is required")
		@Size(max = 120, message = "Name must have 120 characters or less")
		String name,

		@Size(max = 600, message = "Description must have 600 characters or less")
		String description,

		@NotNull(message = "Price is required")
		@DecimalMin(value = "0.01", message = "Price must be greater than 0")
		BigDecimal price,

		@NotNull(message = "Stock is required")
		@Min(value = 0, message = "Stock cannot be negative")
		Integer stock,

		@Size(max = 500, message = "Image URL must have 500 characters or less")
		String imageUrl,

		@NotNull(message = "Category id is required")
		Long categoryId,

		Boolean active
) {
}
