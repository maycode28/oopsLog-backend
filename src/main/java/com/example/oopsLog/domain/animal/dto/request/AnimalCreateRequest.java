package com.example.oopsLog.domain.animal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AnimalCreateRequest(
        @NotBlank(message = "동물 이름은 필수입니다.")
        String name,
        String description,
        String imageUrl,
        @Min(value = 1, message = "requiredSaveCount는 1 이상이어야 합니다.")
        Integer requiredSaveCount
) {
}

