package com.petshop.backend.dto.response;

public record UserProfileUpdateResponse(UserProfileResponse profile, String token) {
}
