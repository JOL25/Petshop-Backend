package com.petshop.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.petshop.backend.dto.request.ProductRequest;
import com.petshop.backend.dto.response.ProductResponse;
import com.petshop.backend.entity.Category;
import com.petshop.backend.entity.Product;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.repository.CategoryRepository;
import com.petshop.backend.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CategoryRepository categoryRepository;

	private ProductService productService;

	@BeforeEach
	void setUp() {
		productService = new ProductService(productRepository, categoryRepository);
	}

	@Test
	void createsProductUsingActiveCategory() {
		Category category = Category.builder()
				.id(1L)
				.name("Juguetes")
				.active(true)
				.build();
		ProductRequest request = new ProductRequest(
				"Pelota",
				"Pelota resistente",
				new BigDecimal("19.90"),
				10,
				"https://example.com/pelota.jpg",
				1L,
				null
		);

		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
			Product product = invocation.getArgument(0);
			product.setId(10L);
			return product;
		});

		ProductResponse response = productService.createProduct(request);

		assertEquals(10L, response.id());
		assertEquals("Pelota", response.name());
		assertEquals(new BigDecimal("19.90"), response.price());
		assertEquals(1L, response.categoryId());
		assertEquals("Juguetes", response.categoryName());
		assertEquals(true, response.active());
	}

	@Test
	void rejectsProductWhenCategoryIsInactive() {
		Category category = Category.builder()
				.id(1L)
				.name("Juguetes")
				.active(false)
				.build();
		ProductRequest request = new ProductRequest(
				"Pelota",
				null,
				new BigDecimal("19.90"),
				10,
				null,
				1L,
				true
		);

		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		assertThrows(BadRequestException.class, () -> productService.createProduct(request));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	void deactivatesActiveProduct() {
		Product product = Product.builder()
				.id(8L)
				.name("Shampoo")
				.active(true)
				.build();
		when(productRepository.findById(8L)).thenReturn(Optional.of(product));

		productService.deactivateProduct(8L);

		assertFalse(product.getActive());
		verify(productRepository).save(product);
	}
}
