package com.petshop.backend.controller;

import com.petshop.backend.dto.request.UpdateOrderStatusRequest;
import com.petshop.backend.dto.response.OrderResponse;
import com.petshop.backend.entity.OrderStatus;
import com.petshop.backend.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin", description = "Administracion de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> getOrders(@RequestParam(required = false) OrderStatus status) {
		return ResponseEntity.ok(orderService.getAllOrders(status));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.getOrderByIdForAdmin(id));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<OrderResponse> updateOrderStatus(
			@PathVariable Long id,
			@Valid @RequestBody UpdateOrderStatusRequest request
	) {
		return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
	}
}
