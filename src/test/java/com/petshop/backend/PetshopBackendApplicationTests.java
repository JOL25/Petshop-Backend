package com.petshop.backend;

import com.petshop.backend.repository.UserRepository;
import com.petshop.backend.repository.CategoryRepository;
import com.petshop.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
				"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
				"org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
		"app.jwt.secret=petshop_backend_test_secret_key_with_more_than_32_chars",
		"app.jwt.expiration=86400000"
})
class PetshopBackendApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private ProductRepository productRepository;

	@MockitoBean
	private CategoryRepository categoryRepository;

	@Test
	void contextLoads() {
	}

}
