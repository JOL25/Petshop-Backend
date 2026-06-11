package com.petshop.backend.service;

import com.petshop.backend.dto.request.ProductRequest;
import com.petshop.backend.dto.response.ProductResponse;
import com.petshop.backend.entity.Category;
import com.petshop.backend.entity.Product;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.exception.ResourceNotFoundException;
import com.petshop.backend.repository.CategoryRepository;
import com.petshop.backend.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> getActiveProducts(Long categoryId) {
		List<Product> products = categoryId == null
				? productRepository.findByActiveTrueOrderByNameAsc()
				: productRepository.findByCategoryIdAndActiveTrueOrderByNameAsc(categoryId);

		return products.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse getActiveProductById(Long id) {
		Product product = productRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		return toResponse(product);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> getAllProductsForAdmin() {
		return productRepository.findAllByOrderByNameAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public ProductResponse createProduct(ProductRequest request) {
		Category category = getActiveCategory(request.categoryId());

		Product product = Product.builder()
				.name(request.name())
				.description(request.description())
				.price(request.price())
				.stock(request.stock())
				.imageUrl(request.imageUrl())
				.category(category)
				.active(request.active() == null || request.active())
				.build();

		return toResponse(productRepository.save(product));
	}

	@Transactional
	public ProductResponse updateProduct(Long id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		Category category = getActiveCategory(request.categoryId());

		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setStock(request.stock());
		product.setImageUrl(request.imageUrl());
		product.setCategory(category);
		if (request.active() != null) {
			product.setActive(request.active());
		}

		return toResponse(productRepository.save(product));
	}

	@Transactional
	public void deactivateProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new BadRequestException("Product is already inactive");
		}

		product.setActive(false);
		productRepository.save(product);
	}

	private Category getActiveCategory(Long categoryId) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		if (!category.getActive()) {
			throw new BadRequestException("Category is inactive");
		}

		return category;
	}

	private ProductResponse toResponse(Product product) {
		Category category = product.getCategory();

		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStock(),
				product.getImageUrl(),
				product.getActive(),
				category.getId(),
				category.getName(),
				product.getCreatedAt(),
				product.getUpdatedAt()
		);
	}
}
