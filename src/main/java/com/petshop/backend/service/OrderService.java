package com.petshop.backend.service;

import com.petshop.backend.dto.request.CreateOrderRequest;
import com.petshop.backend.dto.request.OrderItemRequest;
import com.petshop.backend.dto.request.UpdateOrderStatusRequest;
import com.petshop.backend.dto.response.OrderItemResponse;
import com.petshop.backend.dto.response.OrderResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.OrderEntity;
import com.petshop.backend.entity.OrderItem;
import com.petshop.backend.entity.OrderStatus;
import com.petshop.backend.entity.Product;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.exception.ResourceNotFoundException;
import com.petshop.backend.repository.OrderRepository;
import com.petshop.backend.repository.ProductRepository;
import com.petshop.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	public OrderService(
			OrderRepository orderRepository,
			ProductRepository productRepository,
			UserRepository userRepository
	) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public OrderResponse createOrder(String userEmail, CreateOrderRequest request) {
		AppUser user = getUserByEmail(userEmail);
		Map<Long, Integer> requestedQuantities = mergeQuantities(request.items());

		OrderEntity order = OrderEntity.builder()
				.user(user)
				.status(OrderStatus.PENDING)
				.total(BigDecimal.ZERO)
				.build();

		BigDecimal total = BigDecimal.ZERO;

		for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
			Product product = getActiveProduct(entry.getKey());
			Integer quantity = entry.getValue();
			validateAvailableStock(product, quantity);

			BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
			OrderItem item = OrderItem.builder()
					.order(order)
					.product(product)
					.quantity(quantity)
					.unitPrice(product.getPrice())
					.subtotal(subtotal)
					.build();

			order.getItems().add(item);
			total = total.add(subtotal);
		}

		order.setTotal(total);
		return toResponse(orderRepository.save(order));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> getMyOrders(String userEmail) {
		AppUser user = getUserByEmail(userEmail);

		return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getMyOrderById(String userEmail, Long orderId) {
		AppUser user = getUserByEmail(userEmail);
		OrderEntity order = orderRepository.findByIdAndUserId(orderId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> getAllOrders(OrderStatus status) {
		List<OrderEntity> orders = status == null
				? orderRepository.findAll()
				: orderRepository.findByStatusOrderByCreatedAtDesc(status);

		return orders.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrderByIdForAdmin(Long orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		return toResponse(order);
	}

	@Transactional
	public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
		if (request.status() == OrderStatus.PAID) {
			throw new BadRequestException("Paid status can only be confirmed by the payment provider");
		}

		return changeOrderStatus(orderId, request.status());
	}

	@Transactional
	public OrderResponse confirmPayment(Long orderId) {
		return changeOrderStatus(orderId, OrderStatus.PAID);
	}

	private OrderResponse changeOrderStatus(Long orderId, OrderStatus nextStatus) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		OrderStatus previousStatus = order.getStatus();

		if (previousStatus == nextStatus) {
			return toResponse(order);
		}

		if (previousStatus == OrderStatus.CANCELLED) {
			throw new BadRequestException("Cancelled orders cannot change status");
		}

		if (nextStatus == OrderStatus.PAID && previousStatus != OrderStatus.PAID) {
			discountStock(order);
		}

		order.setStatus(nextStatus);
		return toResponse(orderRepository.save(order));
	}

	private Map<Long, Integer> mergeQuantities(List<OrderItemRequest> items) {
		Map<Long, Integer> quantities = new LinkedHashMap<>();

		for (OrderItemRequest item : items) {
			quantities.merge(item.productId(), item.quantity(), Integer::sum);
		}

		return quantities;
	}

	private void discountStock(OrderEntity order) {
		for (OrderItem item : order.getItems()) {
			Product product = item.getProduct();
			validateAvailableStock(product, item.getQuantity());
			product.setStock(product.getStock() - item.getQuantity());
		}
	}

	private Product getActiveProduct(Long productId) {
		return productRepository.findByIdAndActiveTrue(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
	}

	private AppUser getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private void validateAvailableStock(Product product, Integer quantity) {
		if (product.getStock() < quantity) {
			throw new BadRequestException("Insufficient stock for product: " + product.getName());
		}
	}

	private OrderResponse toResponse(OrderEntity order) {
		return new OrderResponse(
				order.getId(),
				order.getUser().getId(),
				order.getUser().getEmail(),
				order.getStatus(),
				order.getTotal(),
				order.getItems().stream().map(this::toItemResponse).toList(),
				order.getCreatedAt(),
				order.getUpdatedAt()
		);
	}

	private OrderItemResponse toItemResponse(OrderItem item) {
		Product product = item.getProduct();

		return new OrderItemResponse(
				item.getId(),
				product.getId(),
				product.getName(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getSubtotal()
		);
	}
}
