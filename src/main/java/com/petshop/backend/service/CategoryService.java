package com.petshop.backend.service;

import com.petshop.backend.dto.request.CategoryRequest;
import com.petshop.backend.dto.response.CategoryResponse;
import com.petshop.backend.entity.Category;
import com.petshop.backend.exception.BadRequestException;
import com.petshop.backend.exception.ResourceNotFoundException;
import com.petshop.backend.repository.CategoryRepository;
import com.petshop.backend.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> getActiveCategories() {
		return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> getAllCategoriesForAdmin() {
		return categoryRepository.findAllByOrderByNameAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public CategoryResponse getCategoryById(Long id) {
		return categoryRepository.findById(id)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
	}

	@Transactional
	public CategoryResponse createCategory(CategoryRequest request) {
		validateUniqueName(request.name(), null);

		Category category = Category.builder()
				.name(request.name())
				.description(request.description())
				.active(request.active() == null || request.active())
				.build();

		return toResponse(categoryRepository.save(category));
	}

	@Transactional
	public CategoryResponse updateCategory(Long id, CategoryRequest request) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		validateUniqueName(request.name(), id);

		category.setName(request.name());
		category.setDescription(request.description());
		if (request.active() != null) {
			category.setActive(request.active());
		}

		return toResponse(categoryRepository.save(category));
	}

	@Transactional
	public void deactivateCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		if (!category.getActive()) {
			throw new BadRequestException("Category is already inactive");
		}

		category.setActive(false);
		categoryRepository.save(category);
	}

	@Transactional
	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		if (productRepository.existsByCategoryId(id)) {
			category.setActive(false);
			categoryRepository.save(category);
			return;
		}

		categoryRepository.delete(category);
	}

	private void validateUniqueName(String name, Long currentCategoryId) {
		categoryRepository.findByNameIgnoreCase(name)
				.filter(category -> currentCategoryId == null || !category.getId().equals(currentCategoryId))
				.ifPresent(category -> {
					throw new BadRequestException("Category name is already registered");
				});
	}

	private CategoryResponse toResponse(Category category) {
		return new CategoryResponse(
				category.getId(),
				category.getName(),
				category.getDescription(),
				category.getActive(),
				category.getCreatedAt(),
				category.getUpdatedAt()
		);
	}
}
