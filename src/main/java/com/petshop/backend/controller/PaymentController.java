package com.petshop.backend.controller;

import com.petshop.backend.dto.request.CreatePaymentPreferenceRequest;
import com.petshop.backend.dto.response.PaymentPreferenceResponse;
import com.petshop.backend.dto.response.PaymentResponse;
import com.petshop.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Pagos", description = "Preferencias de pago y webhook de Mercado Pago")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/create-preference")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PaymentPreferenceResponse> createPreference(
			Principal principal,
			@Valid @RequestBody CreatePaymentPreferenceRequest request
	) {
		return ResponseEntity.ok(paymentService.createPreference(principal.getName(), request));
	}

	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(
			@RequestBody(required = false) Map<String, Object> body,
			@RequestParam Map<String, String> params
	) {
		paymentService.handleWebhook(body == null ? Collections.emptyMap() : body, params);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PaymentResponse> getPayment(Principal principal, @PathVariable Long id) {
		return ResponseEntity.ok(paymentService.getPayment(principal.getName(), id));
	}
}
