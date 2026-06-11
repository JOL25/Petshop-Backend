package com.petshop.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient mercadoPagoRestClient() {
		return RestClient.builder()
				.baseUrl("https://api.mercadopago.com")
				.build();
	}
}
