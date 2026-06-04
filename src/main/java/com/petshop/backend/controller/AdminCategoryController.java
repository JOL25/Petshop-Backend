package com.petshop.backend.controller;

import com.petshop.backend.dto.request.CategoryRequest;
import com.petshop.backend.dto.response.CategoryResponse;
import com.petshop.backend.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

	private final CategoryService categoryService;

	public AdminCategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public ResponseEntity<List<CategoryResponse>> getCategories() {
		return ResponseEntity.ok(categoryService.getAllCategoriesForAdmin());
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
		return ResponseEntity.ok(categoryService.getCategoryById(id));
	}

	@PostMapping
	public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponse> updateCategory(
			@PathVariable Long id,
			@Valid @RequestBody CategoryRequest request
	) {
		return ResponseEntity.ok(categoryService.updateCategory(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
		categoryService.deactivateCategory(id);
		return ResponseEntity.noContent().build();
	}
}
