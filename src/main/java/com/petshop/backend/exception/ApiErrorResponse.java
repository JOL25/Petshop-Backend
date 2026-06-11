package com.petshop.backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> details
) {

	public static ApiErrorResponse of(int status, String error, String message, String path) {
		return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, null);
	}

	public static ApiErrorResponse of(
			int status,
			String error,
			String message,
			String path,
			Map<String, String> details
	) {
		return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, details);
	}
}
