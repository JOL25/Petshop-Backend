package com.petshop.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.petshop.backend.config.SecurityConfig;
import com.petshop.backend.security.CustomUserDetailsService;
import com.petshop.backend.security.JwtAuthenticationFilter;
import com.petshop.backend.security.JwtService;
import com.petshop.backend.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminEndpointSecurityTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductService productService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@Test
	void rejectsAnonymousRequestsToAdminEndpoints() throws Exception {
		mockMvc.perform(get("/api/admin/products"))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsClientRoleFromAdminEndpoints() throws Exception {
		mockMvc.perform(get("/api/admin/products").with(user("client").roles("CLIENT")))
				.andExpect(status().isForbidden());
	}

	@Test
	void allowsAdminRoleToAccessAdminEndpoints() throws Exception {
		when(productService.getAllProductsForAdmin()).thenReturn(List.of());

		mockMvc.perform(get("/api/admin/products").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());
	}
}
