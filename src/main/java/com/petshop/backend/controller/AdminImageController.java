package com.petshop.backend.controller;

import com.petshop.backend.dto.response.ImageUploadResponse;
import com.petshop.backend.service.ProductImageStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
@Tag(name = "Admin", description = "Carga de recursos del catalogo")
@SecurityRequirement(name = "bearerAuth")
public class AdminImageController {

	private final ProductImageStorageService productImageStorageService;

	public AdminImageController(ProductImageStorageService productImageStorageService) {
		this.productImageStorageService = productImageStorageService;
	}

	@PostMapping(value = "/products", consumes = "multipart/form-data")
	public ResponseEntity<ImageUploadResponse> uploadProductImage(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(productImageStorageService.uploadProductImage(file));
	}
}
