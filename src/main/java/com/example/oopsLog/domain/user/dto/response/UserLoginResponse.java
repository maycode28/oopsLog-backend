package com.example.oopsLog.domain.user.dto.response;

import com.example.oopsLog.domain.user.entity.User;

public record UserLoginResponse(
        Long userId,
        String loginId,
        String nickname
) {
    public static UserLoginResponse from(User user) {
        return new UserLoginResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getNickname()
        );
    }
}

