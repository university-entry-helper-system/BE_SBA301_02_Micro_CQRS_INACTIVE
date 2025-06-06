package com.example.userservice.service;

import com.example.userservice.dto.request.AccountCreationRequest;
import com.example.userservice.dto.request.LoginRequest;
import com.example.userservice.dto.request.RefreshTokenRequest;
import com.example.userservice.dto.response.AuthResponse;

public interface AuthService {
    void registerUser(AccountCreationRequest request);
    void activateAccount(String email, String code);
    AuthResponse authenticate(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void requestPasswordReset(String email);
    void resetPassword(String email, String token, String newPassword);
    void logout(String refreshToken); // Logout the current refresh token
    boolean existByUsername(String username);
    boolean existByEmail(String email);
}