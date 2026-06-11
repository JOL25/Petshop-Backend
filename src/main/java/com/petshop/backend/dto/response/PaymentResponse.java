package com.petshop.backend.dto.response;

import com.petshop.backend.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
		Long id,
		Long orderId,
		String provider,
		String providerPaymentId,
		String providerPreferenceId,
		PaymentStatus status,
		BigDecimal amount,
		String checkoutUrl,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
