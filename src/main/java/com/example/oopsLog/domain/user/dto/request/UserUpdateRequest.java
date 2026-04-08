package com.example.oopsLog.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,
        LocalDate birthDate,
        String phoneNumber,
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
) {
}

