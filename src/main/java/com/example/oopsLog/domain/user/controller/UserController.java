package com.example.oopsLog.domain.user.controller;

import com.example.oopsLog.common.response.ApiResponse;
import com.example.oopsLog.domain.user.dto.request.UserCreateRequest;
import com.example.oopsLog.domain.user.dto.request.UserUpdateRequest;
import com.example.oopsLog.domain.user.dto.response.UserResponse;
import com.example.oopsLog.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = UserResponse.from(userService.create(request));
        return ResponseEntity.created(URI.create("/api/users/" + response.userId()))
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAll().stream().map(UserResponse::from).toList()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(userService.findById(userId))));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(userService.update(userId, request))));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

