package com.petshop.backend.service;

import com.petshop.backend.dto.response.ImageUploadResponse;
import com.petshop.backend.exception.BadRequestException;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorageService {

	private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	private final RestClient restClient;
	private final String supabaseUrl;
	private final String serviceRoleKey;
	private final String bucket;
	private final long maxFileSize;

	public ProductImageStorageService(
			@Value("${app.storage.supabase-url:}") String supabaseUrl,
			@Value("${app.storage.service-role-key:}") String serviceRoleKey,
			@Value("${app.storage.bucket:product-images}") String bucket,
			@Value("${app.storage.max-file-size:5242880}") long maxFileSize
	) {
		this.restClient = RestClient.create();
		this.supabaseUrl = stripTrailingSlash(supabaseUrl);
		this.serviceRoleKey = serviceRoleKey;
		this.bucket = bucket;
		this.maxFileSize = maxFileSize;
	}

	public ImageUploadResponse uploadProductImage(MultipartFile file) {
		validate(file);
		String extension = extensionFor(file.getContentType());
		String objectPath = "products/" + YearMonth.now() + "/" + UUID.randomUUID() + extension;

		try {
			restClient.post()
					.uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath)
					.contentType(MediaType.parseMediaType(file.getContentType()))
					.header("Authorization", "Bearer " + serviceRoleKey)
					.header("apikey", serviceRoleKey)
					.header("x-upsert", "false")
					.body(file.getBytes())
					.retrieve()
					.toBodilessEntity();
		} catch (Exception exception) {
			throw new BadRequestException("Could not upload the product image");
		}

		return new ImageUploadResponse(supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath);
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("An image file is required");
		}
		if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(serviceRoleKey)) {
			throw new BadRequestException("Image storage is not configured");
		}
		if (!ALLOWED_TYPES.contains(file.getContentType())) {
			throw new BadRequestException("Only JPG, PNG and WEBP images are allowed");
		}
		if (file.getSize() > maxFileSize) {
			throw new BadRequestException("Image must be 5 MB or smaller");
		}
	}

	private String extensionFor(String contentType) {
		return switch (contentType) {
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			default -> ".webp";
		};
	}

	private String stripTrailingSlash(String value) {
		return value == null ? "" : value.replaceFirst("/+$", "");
	}
}
