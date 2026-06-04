package com.petshop.backend.controller;

import com.petshop.backend.dto.response.CategoryResponse;
import com.petshop.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorias", description = "Categorias publicas activas")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public ResponseEntity<List<CategoryResponse>> getCategories() {
		return ResponseEntity.ok(categoryService.getActiveCategories());
	}
}
