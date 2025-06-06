package com.example.userservice.service.impl;

import com.example.userservice.dto.request.AccountCreationRequest;
import com.example.userservice.dto.request.UpdatePasswordRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.AccountResponse;
import com.example.userservice.dto.response.PageResponse;
import com.example.userservice.dto.response.RoleResponse;
import com.example.userservice.entity.Account;
import com.example.userservice.entity.Role;
import com.example.userservice.repository.AccountRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.service.AccountService;
import com.example.userservice.util.RoleConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountServiceImpl implements AccountService {

    AccountRepository accountRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    // Helper method to map Account entity to AccountResponse DTO
    private AccountResponse mapAccountToAccountResponse(Account account) {
        if (account == null) return null;
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getFullName(),
                account.getPhone(),
                account.getStatus(),
                new RoleResponse(account.getRole().getId(), account.getRole().getName()),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public AccountResponse createGeneralUser(AccountCreationRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists.");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        Role userRole = roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found."));

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newAccount.setFullName(request.getFullName());
        newAccount.setPhone(request.getPhone());
        newAccount.setRole(userRole);
        newAccount.setStatus("ACTIVE"); // Admin tạo thì có thể ACTIVE luôn

        Account savedAccount = accountRepository.save(newAccount);
        log.info("Admin created general user: {}", savedAccount.getUsername());
        return mapAccountToAccountResponse(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponse createAdminUser(AccountCreationRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists.");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found."));

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newAccount.setFullName(request.getFullName());
        newAccount.setPhone(request.getPhone());
        newAccount.setRole(adminRole);
        newAccount.setStatus("ACTIVE");

        Account savedAccount = accountRepository.save(newAccount);
        log.info("Admin created new admin user: {}", savedAccount.getUsername());
        return mapAccountToAccountResponse(savedAccount);
    }

    @Override
    public PageResponse<AccountResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Account> accountPage = accountRepository.findAll(pageable);

        List<AccountResponse> content = accountPage.getContent().stream()
                .map(this::mapAccountToAccountResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.isLast(),
                accountPage.isFirst(),
                accountPage.isEmpty()
        );
    }

    @Override
    public PageResponse<AccountResponse> getUsersBySearch(String name, int page, int size) {
        // Tùy thuộc vào yêu cầu, bạn có thể tìm kiếm theo username, fullName hoặc cả hai
        // Ví dụ này tìm theo username (cần thêm method trong repository)
        // Hoặc tạo Custom Repository method với @Query
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Giả sử có một method findByUsernameContainingIgnoreCase trong AccountRepository
        // Nếu không, bạn cần tạo một query riêng hoặc sử dụng Querydsl/Criteria API
        Page<Account> accountPage = accountRepository.findByUsernameContainingIgnoreCase(name, pageable);
        // Hoặc nếu muốn tìm cả theo fullName
        // Page<Account> accountPage = accountRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(name, name, pageable);


        List<AccountResponse> content = accountPage.getContent().stream()
                .map(this::mapAccountToAccountResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.isLast(),
                accountPage.isFirst(),
                accountPage.isEmpty()
        );
    }

    @Override
    public AccountResponse getUserById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        return mapAccountToAccountResponse(account);
    }

    @Override
    @Transactional
    public void deleteUser(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new RuntimeException("Account not found with ID: " + accountId);
        }
        accountRepository.deleteById(accountId);
        log.info("Account with ID {} deleted.", accountId);
    }

    @Override
    @Transactional
    public AccountResponse updateUser(UUID accountId, UserUpdateRequest request) {
        Account existingAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        // Cập nhật thông tin nếu có thay đổi
        if (request.getFullName() != null) existingAccount.setFullName(request.getFullName());
        if (request.getPhone() != null) existingAccount.setPhone(request.getPhone());
        if (request.getEmail() != null && !request.getEmail().equals(existingAccount.getEmail())) {
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use.");
            }
            existingAccount.setEmail(request.getEmail());
            // Nếu email thay đổi, có thể cần đặt lại trạng thái PENDING_VERIFICATION và gửi email xác minh mới
            // existingAccount.setStatus("PENDING_VERIFICATION");
            // mailService.sendVerificationEmail(existingAccount.getEmail(), ...);
        }

        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account with ID {} updated.", accountId);
        return mapAccountToAccountResponse(updatedAccount);
    }

    @Override
    @Transactional
    public AccountResponse setRole(UUID accountId, List<Long> roleIds) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        if (roleIds == null || roleIds.isEmpty()) {
            throw new RuntimeException("Role IDs cannot be empty.");
        }

        // Lấy tất cả các roles hợp lệ từ DB
        List<Role> newRoles = roleRepository.findAllByIdIn(roleIds);

        if (newRoles.size() != roleIds.size()) {
            throw new RuntimeException("One or more specified roles do not exist.");
        }

        // Hiện tại Account chỉ có 1 Role (ManyToOne), nên ta chỉ lấy role đầu tiên
        // Nếu muốn hỗ trợ nhiều vai trò (ManyToMany), bạn cần thay đổi Entity và logic
        if (!newRoles.isEmpty()) {
            account.setRole(newRoles.get(0)); // Gán vai trò đầu tiên từ danh sách
        } else {
            throw new RuntimeException("No valid roles provided.");
        }

        Account updatedAccount = accountRepository.save(account);
        log.info("Role(s) for account {} updated.", accountId);
        return mapAccountToAccountResponse(updatedAccount);
    }


    @Override
    public AccountResponse getMyInfo(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found."));
        return mapAccountToAccountResponse(account);
    }

    @Override
    @Transactional
    public void updatePassword(String username, UpdatePasswordRequest updatePasswordRequest) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Old password does not match.");
        }

        // Cập nhật mật khẩu mới
        account.setPasswordHash(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        accountRepository.save(account);
        log.info("Password for account {} updated successfully.", username);
    }
}