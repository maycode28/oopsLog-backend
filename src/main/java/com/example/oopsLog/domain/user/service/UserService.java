package com.example.oopsLog.domain.user.service;

import com.example.oopsLog.domain.user.dto.request.UserCreateRequest;
import com.example.oopsLog.domain.user.dto.request.UserUpdateRequest;
import com.example.oopsLog.domain.user.entity.User;
import com.example.oopsLog.domain.user.repository.UserRepository;
import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(UserCreateRequest request) {
        User user = new User(
                request.loginId(),
                request.password(),
                request.name(),
                request.nickname(),
                request.birthDate(),
                request.phoneNumber(),
                request.email()
        );
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User update(Long userId, UserUpdateRequest request) {
        User user = findById(userId);
        user.updateProfile(
                request.name(),
                request.nickname(),
                request.birthDate(),
                request.phoneNumber(),
                request.email()
        );
        return user;
    }

    public void delete(Long userId) {
        userRepository.delete(findById(userId));
    }
}

