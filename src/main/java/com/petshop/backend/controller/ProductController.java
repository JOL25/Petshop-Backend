package com.petshop.backend.controller;

import com.petshop.backend.dto.response.ProductResponse;
import com.petshop.backend.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Productos", description = "Catalogo publico de productos activos")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam(required = false) Long categoryId) {
		return ResponseEntity.ok(productService.getActiveProducts(categoryId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getActiveProductById(id));
	}
}
