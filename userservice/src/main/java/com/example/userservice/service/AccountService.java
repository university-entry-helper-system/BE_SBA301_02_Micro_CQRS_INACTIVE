package com.example.userservice.service;

import com.example.userservice.dto.request.AccountCreationRequest;
import com.example.userservice.dto.request.UpdatePasswordRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.AccountResponse;
import com.example.userservice.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountResponse createGeneralUser(AccountCreationRequest request);
    AccountResponse createAdminUser(AccountCreationRequest request);
    PageResponse<AccountResponse> getAllUsers(int page, int size);
    PageResponse<AccountResponse> getUsersBySearch(String name, int page, int size);
    AccountResponse getUserById(UUID accountId);
    void deleteUser(UUID accountId);
    AccountResponse updateUser(UUID accountId, UserUpdateRequest request);
    AccountResponse setRole(UUID accountId, List<Long> roleIds);
    AccountResponse getMyInfo(String username);
    void updatePassword(String username, UpdatePasswordRequest updatePasswordRequest);
}