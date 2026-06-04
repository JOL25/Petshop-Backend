package com.petshop.backend.controller;

import com.petshop.backend.dto.response.CategoryResponse;
import com.petshop.backend.service.CategoryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
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
