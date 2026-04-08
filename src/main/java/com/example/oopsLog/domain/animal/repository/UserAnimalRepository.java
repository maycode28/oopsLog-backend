package com.example.oopsLog.domain.animal.repository;

import com.example.oopsLog.domain.animal.entity.UserAnimal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAnimalRepository extends JpaRepository<UserAnimal, Long> {
    List<UserAnimal> findByUserUserId(Long userId);
}

