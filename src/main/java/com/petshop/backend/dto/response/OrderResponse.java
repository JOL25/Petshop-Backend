package com.petshop.backend.dto.response;

import com.petshop.backend.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
		Long id,
		Long userId,
		String userEmail,
		OrderStatus status,
		BigDecimal total,
		List<OrderItemResponse> items,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
