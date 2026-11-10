package com.pulsepass.service;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;

// PRD §18: contrato mínimo de UserService
public interface UserService {
    UserResponse register(RegisterUserRequest request);
    UserResponse findByEmail(String email);
    UserResponse findByUsername(String username);
}
