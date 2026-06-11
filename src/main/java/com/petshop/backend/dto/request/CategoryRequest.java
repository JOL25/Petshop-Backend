package com.petshop.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
		@NotBlank(message = "Name is required")
		@Size(max = 80, message = "Name must have 80 characters or less")
		String name,

		@Size(max = 300, message = "Description must have 300 characters or less")
		String description,

		Boolean active
) {
}
