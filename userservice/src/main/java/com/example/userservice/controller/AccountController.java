package com.example.userservice.controller;

import com.example.userservice.dto.request.AccountCreationRequest;
import com.example.userservice.dto.request.RoleAssignmentRequest;
import com.example.userservice.dto.request.UpdatePasswordRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.AccountResponse;
import com.example.userservice.dto.response.MessageResponse;
import com.example.userservice.dto.response.PageResponse;
import com.example.userservice.service.AccountService; // Dịch vụ quản lý tài khoản
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Quan trọng cho phân quyền
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID; // Sử dụng UUID cho Account ID

@RestController
@RequestMapping("api/v1/accounts") // Đường dẫn gốc cho các API quản lý tài khoản
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountController {

    AccountService accountService; // Dịch vụ quản lý tài khoản

    // --- Admin/Management Endpoints ---

    // Create a new general user (by an admin)
    @Operation(summary = "Create a general user (Admin)", description = "Creates a general user account by an administrator. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "User created successfully.")
    @ApiResponse(responseCode = "400", description = "Bad request due to invalid input.")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin mới có quyền
    @PostMapping("/create-user")
    public ResponseEntity<AccountResponse> createGeneralUser(@RequestBody @Valid AccountCreationRequest request) {
        AccountResponse response = accountService.createGeneralUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Create an admin user (by another admin)
    @Operation(summary = "Create an admin user (Admin)", description = "Creates an admin user account by an administrator. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Admin user created successfully.")
    @ApiResponse(responseCode = "400", description = "Bad request due to invalid input.")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin có quyền tạo admin khác
    @PostMapping("/create-admin")
    public ResponseEntity<AccountResponse> createAdminUser(@RequestBody @Valid AccountCreationRequest request) {
        AccountResponse response = accountService.createAdminUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all users with pagination
    @Operation(summary = "Get all users with pagination", description = "Retrieves a paginated list of all users. Requires ADMIN or CONSULTANT role.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')") // Admin hoặc tư vấn viên có quyền
    @GetMapping
    public ResponseEntity<PageResponse<AccountResponse>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<AccountResponse> response = accountService.getAllUsers(page, size);
        return ResponseEntity.ok(response);
    }

    // Search users by name with pagination
    @Operation(summary = "Search users by name", description = "Allows searching users by name with pagination. Requires ADMIN or CONSULTANT role.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<AccountResponse>> searchUsers(
            @RequestParam String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<AccountResponse> response = accountService.getUsersBySearch(name, page, size);
        return ResponseEntity.ok(response);
    }

    // Get user by ID
    @Operation(summary = "Get user by ID", description = "Retrieves user details based on the user ID. Requires ADMIN/CONSULTANT role or self-access.")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    // @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT') or @securityService.isOwner(#accountId, principal)")
    // Logic isOwner sẽ cần một Spring Bean để kiểm tra, ví dụ: @securityService.isOwner
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')") // Tạm thời chỉ cho phép admin/consultant
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getUserById(@PathVariable UUID accountId) {
        AccountResponse response = accountService.getUserById(accountId);
        return ResponseEntity.ok(response);
    }

    // Delete user
    @Operation(summary = "Delete a user", description = "Deletes a user account. Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID accountId) {
        accountService.deleteUser(accountId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Update user by ID
    @Operation(summary = "Update user by ID", description = "Allows updating user information using their ID. Requires ADMIN role or self-access.")
    @ApiResponse(responseCode = "200", description = "User updated successfully.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    // @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#accountId, principal)")
    @PreAuthorize("hasRole('ADMIN')") // Tạm thời chỉ cho phép admin
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateUser(@PathVariable UUID accountId, @RequestBody @Valid UserUpdateRequest request) {
        AccountResponse response = accountService.updateUser(accountId, request);
        return ResponseEntity.ok(response);
    }

    // Set roles for a user
    @Operation(summary = "Set roles for a user", description = "Allows setting roles for a user account. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Roles assigned successfully.")
    @ApiResponse(responseCode = "404", description = "User or role not found.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{accountId}/set-roles")
    public ResponseEntity<AccountResponse> setRole(@PathVariable UUID accountId, @RequestBody @Valid RoleAssignmentRequest request) { // Dùng DTO cho request
        AccountResponse response = accountService.setRole(accountId, request.getRoleIds());
        return ResponseEntity.ok(response);
    }

    // --- User-Specific Endpoints (Requires Authentication) ---

    // Get current user's info
    @Operation(summary = "Get current user's info", description = "Retrieves the currently authenticated user's information.")
    @ApiResponse(responseCode = "200", description = "Current user info retrieved successfully.")
    @PreAuthorize("isAuthenticated()") // Yêu cầu đã đăng nhập
    @GetMapping("/my-info")
    public ResponseEntity<AccountResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        AccountResponse response = accountService.getMyInfo(userDetails.getUsername()); // Lấy username từ UserDetails
        return ResponseEntity.ok(response);
    }

    // Update password for authenticated user
    @Operation(summary = "Update user password", description = "Allows an authenticated user to update their password.")
    @ApiResponse(responseCode = "200", description = "Password updated successfully.")
    @ApiResponse(responseCode = "400", description = "Bad request due to invalid input (e.g., wrong old password).")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/my-info/update-password") // Endpoint rõ ràng hơn
    public ResponseEntity<MessageResponse> updateMyPassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        accountService.updatePassword(userDetails.getUsername(), updatePasswordRequest);
        return ResponseEntity.ok(new MessageResponse("Password updated successfully."));
    }
}