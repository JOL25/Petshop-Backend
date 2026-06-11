package com.petshop.backend.dto.response;

import com.petshop.backend.entity.PaymentStatus;
import java.math.BigDecimal;

public record PaymentPreferenceResponse(
		Long paymentId,
		Long orderId,
		PaymentStatus status,
		BigDecimal amount,
		String provider,
		String providerPreferenceId,
		String checkoutUrl
) {
}
