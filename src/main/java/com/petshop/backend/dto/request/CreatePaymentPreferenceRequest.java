package com.petshop.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentPreferenceRequest(
		@NotNull(message = "Order id is required")
		Long orderId
) {
}
