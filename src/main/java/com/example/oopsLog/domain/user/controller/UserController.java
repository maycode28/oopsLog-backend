package com.example.oopsLog.domain.user.controller;

import com.example.oopsLog.common.response.ApiResponse;
import com.example.oopsLog.domain.user.dto.request.UserCreateRequest;
import com.example.oopsLog.domain.user.dto.request.UserLoginRequest;
import com.example.oopsLog.domain.user.dto.request.UserUpdateRequest;
import com.example.oopsLog.domain.user.dto.response.UserLoginResponse;
import com.example.oopsLog.domain.user.dto.response.UserResponse;
import com.example.oopsLog.domain.user.service.UserService;
import com.example.oopsLog.common.auth.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = UserResponse.from(userService.signUp(request));
        return ResponseEntity.created(URI.create("/api/users/" + response.userId()))
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        var user = userService.login(request);
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SessionConst.LOGIN_USER_ID, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(UserLoginResponse.from(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.success(null));
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
