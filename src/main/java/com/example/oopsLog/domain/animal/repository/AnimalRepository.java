package com.example.oopsLog.domain.animal.repository;

import com.example.oopsLog.domain.animal.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
}

