package com.example.oopsLog.domain.animal.service;

import com.example.oopsLog.domain.animal.dto.request.AnimalCreateRequest;
import com.example.oopsLog.domain.animal.dto.request.AnimalUpdateRequest;
import com.example.oopsLog.domain.animal.entity.Animal;
import com.example.oopsLog.domain.animal.repository.AnimalRepository;
import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public Animal create(AnimalCreateRequest request) {
        Animal animal = new Animal(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.requiredSaveCount()
        );
        return animalRepository.save(animal);
    }

    public List<Animal> findAll() {
        return animalRepository.findAll();
    }

    public Animal findById(Long animalId) {
        return animalRepository.findById(animalId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANIMAL_NOT_FOUND));
    }

    public Animal update(Long animalId, AnimalUpdateRequest request) {
        Animal animal = findById(animalId);
        animal.update(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.requiredSaveCount()
        );
        return animal;
    }

    public void delete(Long animalId) {
        animalRepository.delete(findById(animalId));
    }
}

