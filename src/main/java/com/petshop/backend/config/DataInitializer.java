package com.petshop.backend.config;

import com.petshop.backend.entity.Category;
import com.petshop.backend.repository.CategoryRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private final CategoryRepository categoryRepository;

	public DataInitializer(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
	public void run(String... args) {
		createCategoryIfMissing("Cosmeticos", "Productos de higiene, cuidado y belleza para mascotas");
		createCategoryIfMissing("Ropa", "Prendas y accesorios de vestir para mascotas");
		createCategoryIfMissing("Juguetes", "Juguetes y articulos de entretenimiento para mascotas");
		createCategoryIfMissing("Comida", "Alimentos, snacks y suplementos para mascotas");
	}

	private void createCategoryIfMissing(String name, String description) {
		if (categoryRepository.existsByNameIgnoreCase(name)) {
			return;
		}

		Category category = Category.builder()
				.name(name)
				.description(description)
				.active(true)
				.build();

		categoryRepository.save(category);
	}
}
