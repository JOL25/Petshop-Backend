package com.petshop.backend.config;

import com.petshop.backend.entity.AppUser;
import com.petshop.backend.entity.Category;
import com.petshop.backend.entity.Product;
import com.petshop.backend.entity.Role;
import com.petshop.backend.repository.CategoryRepository;
import com.petshop.backend.repository.ProductRepository;
import com.petshop.backend.repository.UserRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class DataInitializer implements CommandLineRunner {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final boolean seedEnabled;
	private final String adminName;
	private final String adminEmail;
	private final String adminPassword;

	public DataInitializer(
			CategoryRepository categoryRepository,
			ProductRepository productRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.seed.enabled:true}") boolean seedEnabled,
			@Value("${app.seed.admin-name:Administrador Petshop}") String adminName,
			@Value("${app.seed.admin-email:admin@petshop.com}") String adminEmail,
			@Value("${app.seed.admin-password:}") String adminPassword
	) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.seedEnabled = seedEnabled;
		this.adminName = adminName;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (!seedEnabled) {
			return;
		}

		Category cosmetics = getOrCreateCategory(
				"Cosmeticos",
				"Productos de higiene, cuidado y belleza para mascotas"
		);
		Category clothing = getOrCreateCategory(
				"Ropa",
				"Prendas y accesorios de vestir para mascotas"
		);
		Category toys = getOrCreateCategory(
				"Juguetes",
				"Juguetes y articulos de entretenimiento para mascotas"
		);
		Category food = getOrCreateCategory(
				"Comida",
				"Alimentos, snacks y suplementos para mascotas"
		);

		createProductIfMissing(
				"Shampoo avena para mascotas",
				"Shampoo suave para piel sensible, presentacion de 300 ml",
				"24.90",
				25,
				cosmetics
		);
		createProductIfMissing(
				"Polera termica para perro",
				"Polera abrigadora de talla mediana",
				"39.90",
				15,
				clothing
		);
		createProductIfMissing(
				"Pelota dispensadora de premios",
				"Juguete interactivo de goma resistente",
				"19.90",
				30,
				toys
		);
		createProductIfMissing(
				"Alimento premium para perro",
				"Alimento balanceado para perros adultos, bolsa de 3 kg",
				"59.90",
				20,
				food
		);

		createAdminIfConfigured();
	}

	private Category getOrCreateCategory(String name, String description) {
		return categoryRepository.findByNameIgnoreCase(name)
				.orElseGet(() -> categoryRepository.save(Category.builder()
						.name(name)
						.description(description)
						.active(true)
						.build()));
	}

	private void createProductIfMissing(
			String name,
			String description,
			String price,
			int stock,
			Category category
	) {
		if (productRepository.existsByNameIgnoreCase(name)) {
			return;
		}

		Product product = Product.builder()
				.name(name)
				.description(description)
				.price(new BigDecimal(price))
				.stock(stock)
				.category(category)
				.active(true)
				.build();

		productRepository.save(product);
	}

	private void createAdminIfConfigured() {
		if (!StringUtils.hasText(adminPassword) || userRepository.existsByEmail(adminEmail)) {
			return;
		}

		AppUser admin = AppUser.builder()
				.name(adminName)
				.email(adminEmail)
				.password(passwordEncoder.encode(adminPassword))
				.role(Role.ADMIN)
				.enabled(true)
				.build();

		userRepository.save(admin);
	}
}
