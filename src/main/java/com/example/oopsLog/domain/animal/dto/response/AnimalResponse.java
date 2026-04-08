package com.example.oopsLog.domain.animal.dto.response;

import com.example.oopsLog.domain.animal.entity.Animal;

public record AnimalResponse(
        Long animalId,
        String name,
        String description,
        String imageUrl,
        Integer requiredSaveCount
) {
    public static AnimalResponse from(Animal animal) {
        return new AnimalResponse(
                animal.getAnimalId(),
                animal.getName(),
                animal.getDescription(),
                animal.getImageUrl(),
                animal.getRequiredSaveCount()
        );
    }
}

