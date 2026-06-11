package com.petshop.backend.service;

import com.petshop.backend.dto.request.CreatePaymentPreferenceRequest;
import com.petshop.backend.dto.response.PaymentPreferenceResponse;
import com.petshop.backend.dto.response.PaymentResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.OrderEntity;
import com.petshop.backend.entity.OrderStatus;
import com.petshop.backend.entity.Payment;
import com.petshop.backend.entity.PaymentProvider;
import com.petshop.backend.entity.PaymentStatus;
import com.petshop.backend.entity.Role;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.exception.ForbiddenException;
import com.petshop.backend.exception.ResourceNotFoundException;
import com.petshop.backend.repository.OrderRepository;
import com.petshop.backend.repository.PaymentRepository;
import com.petshop.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final OrderService orderService;
	private final RestClient mercadoPagoRestClient;
	private final String mercadoPagoAccessToken;
	private final String currency;
	private final String frontendUrl;
	private final String webhookUrl;

	public PaymentService(
			PaymentRepository paymentRepository,
			OrderRepository orderRepository,
			UserRepository userRepository,
			OrderService orderService,
			RestClient mercadoPagoRestClient,
			@Value("${app.mercadopago.access-token:}") String mercadoPagoAccessToken,
			@Value("${app.payment.currency:PEN}") String currency,
			@Value("${app.payment.frontend-url:http://localhost:5173}") String frontendUrl,
			@Value("${app.payment.webhook-url:}") String webhookUrl
	) {
		this.paymentRepository = paymentRepository;
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.orderService = orderService;
		this.mercadoPagoRestClient = mercadoPagoRestClient;
		this.mercadoPagoAccessToken = mercadoPagoAccessToken;
		this.currency = currency;
		this.frontendUrl = frontendUrl;
		this.webhookUrl = webhookUrl;
	}

	@Transactional
	public PaymentPreferenceResponse createPreference(String userEmail, CreatePaymentPreferenceRequest request) {
		validateMercadoPagoConfigured();
		AppUser user = getUserByEmail(userEmail);
		OrderEntity order = orderRepository.findByIdAndUserId(request.orderId(), user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (order.getStatus() != OrderStatus.PENDING) {
			throw new BadRequestException("Only pending orders can be paid");
		}

		return paymentRepository.findByOrderId(order.getId())
				.filter(payment -> payment.getStatus() == PaymentStatus.PENDING
						&& StringUtils.hasText(payment.getCheckoutUrl()))
				.map(this::toPreferenceResponse)
				.orElseGet(() -> createMercadoPagoPreference(order, user));
	}

	@Transactional
	public void handleWebhook(Map<String, Object> body, Map<String, String> params) {
		validateMercadoPagoConfigured();
		String providerPaymentId = extractProviderPaymentId(body, params);

		if (!StringUtils.hasText(providerPaymentId)) {
			return;
		}

		Map<String, Object> providerPayment = fetchMercadoPagoPayment(providerPaymentId);
		Long orderId = parseLong(providerPayment.get("external_reference"));

		if (orderId == null) {
			throw new BadRequestException("Payment does not include an order reference");
		}

		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
				.or(() -> paymentRepository.findByOrderId(orderId))
				.orElseGet(() -> Payment.builder()
						.order(order)
						.provider(PaymentProvider.MERCADO_PAGO)
						.status(PaymentStatus.PENDING)
						.amount(order.getTotal())
						.build());

		payment.setProviderPaymentId(providerPaymentId);
		payment.setProvider(PaymentProvider.MERCADO_PAGO);
		payment.setStatus(mapMercadoPagoStatus(asString(providerPayment.get("status"))));
		payment.setAmount(parseBigDecimal(providerPayment.get("transaction_amount"), order.getTotal()));
		paymentRepository.save(payment);

		if (payment.getStatus() == PaymentStatus.APPROVED && order.getStatus() != OrderStatus.PAID) {
			orderService.confirmPayment(orderId);
		}
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPayment(String userEmail, Long paymentId) {
		AppUser user = getUserByEmail(userEmail);
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		boolean owner = payment.getOrder().getUser().getId().equals(user.getId());
		boolean admin = user.getRole() == Role.ADMIN;

		if (!owner && !admin) {
			throw new ForbiddenException("You cannot access this payment");
		}

		return toResponse(payment);
	}

	private PaymentPreferenceResponse createMercadoPagoPreference(OrderEntity order, AppUser user) {
		Map<String, Object> requestBody = buildPreferenceRequest(order, user);
		Map<String, Object> response = postMercadoPagoPreference(requestBody);
		String preferenceId = asString(response.get("id"));
		String checkoutUrl = asString(response.get("init_point"));

		if (!StringUtils.hasText(checkoutUrl)) {
			checkoutUrl = asString(response.get("sandbox_init_point"));
		}

		if (!StringUtils.hasText(preferenceId) || !StringUtils.hasText(checkoutUrl)) {
			throw new BadRequestException("Mercado Pago did not return a valid checkout preference");
		}

		Payment payment = paymentRepository.findByOrderId(order.getId())
				.orElseGet(() -> Payment.builder()
						.order(order)
						.provider(PaymentProvider.MERCADO_PAGO)
						.status(PaymentStatus.PENDING)
						.amount(order.getTotal())
						.build());

		payment.setProvider(PaymentProvider.MERCADO_PAGO);
		payment.setStatus(PaymentStatus.PENDING);
		payment.setAmount(order.getTotal());
		payment.setProviderPreferenceId(preferenceId);
		payment.setCheckoutUrl(checkoutUrl);

		return toPreferenceResponse(paymentRepository.save(payment));
	}

	private Map<String, Object> buildPreferenceRequest(OrderEntity order, AppUser user) {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("title", "Pedido #" + order.getId());
		item.put("quantity", 1);
		item.put("currency_id", currency);
		item.put("unit_price", order.getTotal());

		Map<String, Object> payer = new LinkedHashMap<>();
		payer.put("email", user.getEmail());

		Map<String, Object> backUrls = new LinkedHashMap<>();
		backUrls.put("success", frontendUrl + "/payment/success");
		backUrls.put("failure", frontendUrl + "/payment/failure");
		backUrls.put("pending", frontendUrl + "/payment/pending");

		Map<String, Object> requestBody = new LinkedHashMap<>();
		requestBody.put("items", List.of(item));
		requestBody.put("payer", payer);
		requestBody.put("external_reference", order.getId().toString());
		requestBody.put("back_urls", backUrls);
		requestBody.put("auto_return", "approved");

		if (StringUtils.hasText(webhookUrl)) {
			requestBody.put("notification_url", webhookUrl);
		}

		return requestBody;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> postMercadoPagoPreference(Map<String, Object> requestBody) {
		return mercadoPagoRestClient.post()
				.uri("/checkout/preferences")
				.headers(headers -> headers.setBearerAuth(mercadoPagoAccessToken))
				.body(requestBody)
				.retrieve()
				.body(Map.class);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchMercadoPagoPayment(String providerPaymentId) {
		return mercadoPagoRestClient.get()
				.uri("/v1/payments/{paymentId}", providerPaymentId)
				.headers(headers -> headers.setBearerAuth(mercadoPagoAccessToken))
				.retrieve()
				.body(Map.class);
	}

	private String extractProviderPaymentId(Map<String, Object> body, Map<String, String> params) {
		if (params != null) {
			String dataId = params.get("data.id");
			if (StringUtils.hasText(dataId)) {
				return dataId;
			}

			String id = params.get("id");
			if (StringUtils.hasText(id)) {
				return id;
			}
		}

		if (body == null) {
			return null;
		}

		Object data = body.get("data");
		if (data instanceof Map<?, ?> dataMap) {
			String id = asString(dataMap.get("id"));
			if (StringUtils.hasText(id)) {
				return id;
			}
		}

		return asString(body.get("id"));
	}

	private PaymentStatus mapMercadoPagoStatus(String status) {
		if (status == null) {
			return PaymentStatus.PENDING;
		}

		return switch (status.toLowerCase()) {
			case "approved", "accredited" -> PaymentStatus.APPROVED;
			case "rejected" -> PaymentStatus.REJECTED;
			case "cancelled", "canceled" -> PaymentStatus.CANCELLED;
			default -> PaymentStatus.PENDING;
		};
	}

	private AppUser getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private void validateMercadoPagoConfigured() {
		if (!StringUtils.hasText(mercadoPagoAccessToken)) {
			throw new BadRequestException("Mercado Pago access token is not configured");
		}
	}

	private Long parseLong(Object value) {
		if (value == null) {
			return null;
		}

		try {
			return Long.valueOf(value.toString());
		} catch (NumberFormatException exception) {
			throw new BadRequestException("Invalid order reference in payment");
		}
	}

	private BigDecimal parseBigDecimal(Object value, BigDecimal defaultValue) {
		if (value == null) {
			return defaultValue;
		}

		try {
			return new BigDecimal(value.toString());
		} catch (NumberFormatException exception) {
			return defaultValue;
		}
	}

	private String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private PaymentPreferenceResponse toPreferenceResponse(Payment payment) {
		return new PaymentPreferenceResponse(
				payment.getId(),
				payment.getOrder().getId(),
				payment.getStatus(),
				payment.getAmount(),
				payment.getProvider().name(),
				payment.getProviderPreferenceId(),
				payment.getCheckoutUrl()
		);
	}

	private PaymentResponse toResponse(Payment payment) {
		return new PaymentResponse(
				payment.getId(),
				payment.getOrder().getId(),
				payment.getProvider().name(),
				payment.getProviderPaymentId(),
				payment.getProviderPreferenceId(),
				payment.getStatus(),
				payment.getAmount(),
				payment.getCheckoutUrl(),
				payment.getCreatedAt(),
				payment.getUpdatedAt()
		);
	}
}
