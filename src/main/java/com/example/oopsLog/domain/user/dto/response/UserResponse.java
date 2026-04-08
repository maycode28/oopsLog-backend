package com.example.oopsLog.domain.user.dto.response;

import com.example.oopsLog.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String loginId,
        String name,
        String nickname,
        LocalDate birthDate,
        String phoneNumber,
        String email,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getNickname(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}

