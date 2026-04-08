package com.example.oopsLog.domain.animal.entity;

import com.example.oopsLog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_animals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_animal_id")
    private Long userAnimalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(name = "save_count", nullable = false)
    private Integer saveCount;

    @Column(name = "intimacy_score", nullable = false)
    private Integer intimacyScore;

    @Column(name = "is_my_pet", nullable = false)
    private Boolean isMyPet;

    @Column(name = "last_interacted_at")
    private LocalDateTime lastInteractedAt;

    public UserAnimal(
            User user,
            Animal animal,
            Integer saveCount,
            Integer intimacyScore,
            Boolean isMyPet,
            LocalDateTime lastInteractedAt
    ) {
        this.user = user;
        this.animal = animal;
        this.saveCount = saveCount == null ? 0 : saveCount;
        this.intimacyScore = intimacyScore == null ? 0 : intimacyScore;
        this.isMyPet = isMyPet == null ? Boolean.FALSE : isMyPet;
        this.lastInteractedAt = lastInteractedAt;
    }

    public void update(Integer saveCount, Integer intimacyScore, Boolean isMyPet, LocalDateTime lastInteractedAt) {
        this.saveCount = saveCount;
        this.intimacyScore = intimacyScore;
        this.isMyPet = isMyPet;
        this.lastInteractedAt = lastInteractedAt;
    }
}

