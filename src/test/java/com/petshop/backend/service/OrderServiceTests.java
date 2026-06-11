package com.petshop.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.petshop.backend.dto.request.CreateOrderRequest;
import com.petshop.backend.dto.request.OrderItemRequest;
import com.petshop.backend.dto.request.UpdateOrderStatusRequest;
import com.petshop.backend.dto.response.OrderResponse;
import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.OrderEntity;
import com.petshop.backend.entity.OrderStatus;
import com.petshop.backend.entity.Product;
import com.petshop.backend.entity.Role;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.repository.OrderRepository;
import com.petshop.backend.repository.ProductRepository;
import com.petshop.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private UserRepository userRepository;

	private OrderService orderService;

	@BeforeEach
	void setUp() {
		orderService = new OrderService(orderRepository, productRepository, userRepository);
	}

	@Test
	void calculatesTotalAndMergesRepeatedProducts() {
		AppUser user = testUser();
		Product food = product(1L, "Alimento", "25.50", 20);
		Product toy = product(2L, "Pelota", "10.00", 10);
		CreateOrderRequest request = new CreateOrderRequest(List.of(
				new OrderItemRequest(1L, 2),
				new OrderItemRequest(1L, 1),
				new OrderItemRequest(2L, 2)
		));

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(food));
		when(productRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(toy));
		when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
			OrderEntity order = invocation.getArgument(0);
			order.setId(100L);
			return order;
		});

		OrderResponse response = orderService.createOrder(user.getEmail(), request);

		assertEquals(100L, response.id());
		assertEquals(OrderStatus.PENDING, response.status());
		assertEquals(new BigDecimal("96.50"), response.total());
		assertEquals(2, response.items().size());
		assertEquals(3, response.items().get(0).quantity());
		assertEquals(new BigDecimal("76.50"), response.items().get(0).subtotal());
	}

	@Test
	void rejectsOrderWhenStockIsInsufficient() {
		AppUser user = testUser();
		Product product = product(1L, "Alimento", "25.50", 2);
		CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 3)));

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

		BadRequestException exception = assertThrows(
				BadRequestException.class,
				() -> orderService.createOrder(user.getEmail(), request)
		);

		assertEquals("Insufficient stock for product: Alimento", exception.getMessage());
		verify(orderRepository, never()).save(any(OrderEntity.class));
	}

	@Test
	void rejectsPaidStatusFromAdministrativeFlow() {
		BadRequestException exception = assertThrows(
				BadRequestException.class,
				() -> orderService.updateOrderStatus(
						10L,
						new UpdateOrderStatusRequest(OrderStatus.PAID)
				)
		);

		assertEquals(
				"Paid status can only be confirmed by the payment provider",
				exception.getMessage()
		);
		verify(orderRepository, never()).findById(10L);
	}

	@Test
	void confirmsPaymentAndDiscountsStockFromProviderFlow() {
		Product product = product(1L, "Alimento", "25.50", 5);
		OrderEntity order = OrderEntity.builder()
				.id(10L)
				.user(testUser())
				.status(OrderStatus.PENDING)
				.total(new BigDecimal("51.00"))
				.build();
		order.getItems().add(com.petshop.backend.entity.OrderItem.builder()
				.order(order)
				.product(product)
				.quantity(2)
				.unitPrice(new BigDecimal("25.50"))
				.subtotal(new BigDecimal("51.00"))
				.build());

		when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);

		OrderResponse response = orderService.confirmPayment(10L);

		assertEquals(OrderStatus.PAID, response.status());
		assertEquals(3, product.getStock());
	}

	private AppUser testUser() {
		return AppUser.builder()
				.id(5L)
				.name("Cliente")
				.email("cliente@petshop.com")
				.password("encoded")
				.role(Role.CLIENT)
				.enabled(true)
				.build();
	}

	private Product product(Long id, String name, String price, int stock) {
		return Product.builder()
				.id(id)
				.name(name)
				.price(new BigDecimal(price))
				.stock(stock)
				.active(true)
				.build();
	}
}
