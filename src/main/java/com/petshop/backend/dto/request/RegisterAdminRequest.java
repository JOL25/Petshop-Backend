package com.petshop.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAdminRequest(
		@NotBlank(message = "Name is required")
		@Size(max = 120, message = "Name must have 120 characters or less")
		String name,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 150, message = "Email must have 150 characters or less")
		String email,

		@NotBlank(message = "Password is required")
		@Size(min = 8, message = "Password must have at least 8 characters")
		String password
) {
}
