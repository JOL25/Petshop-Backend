package com.petshop.backend.dto.request;

import com.petshop.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
		@NotNull(message = "Status is required")
		OrderStatus status
) {
}
