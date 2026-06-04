package com.petshop.backend.controller;

import com.petshop.backend.dto.request.CreateOrderRequest;
import com.petshop.backend.dto.response.OrderResponse;
import com.petshop.backend.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(
			Principal principal,
			@Valid @RequestBody CreateOrderRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(principal.getName(), request));
	}

	@GetMapping("/my-orders")
	public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
		return ResponseEntity.ok(orderService.getMyOrders(principal.getName()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> getMyOrderById(Principal principal, @PathVariable Long id) {
		return ResponseEntity.ok(orderService.getMyOrderById(principal.getName(), id));
	}
}
