package com.petshop.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
		@NotEmpty(message = "Order must have at least one item")
		List<@Valid OrderItemRequest> items
) {
}
