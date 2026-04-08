package com.example.oopsLog.domain.animal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "animals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animal_id")
    private Long animalId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "required_save_count", nullable = false)
    private Integer requiredSaveCount;

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<UserAnimal> userAnimals = new ArrayList<>();

    public Animal(String name, String description, String imageUrl, Integer requiredSaveCount) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.requiredSaveCount = requiredSaveCount == null ? 5 : requiredSaveCount;
    }

    public void update(String name, String description, String imageUrl, Integer requiredSaveCount) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.requiredSaveCount = requiredSaveCount;
    }
}

