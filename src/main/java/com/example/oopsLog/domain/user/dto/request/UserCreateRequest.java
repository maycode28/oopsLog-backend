package com.example.oopsLog.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateRequest(
        @NotBlank(message = "로그인 아이디는 필수입니다.")
        @Size(max = 50, message = "로그인 아이디 길이는 50자를 초과할 수 없습니다.")
        String loginId,
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,
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

