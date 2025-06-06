package com.example.userservice.security;

import com.example.userservice.entity.Account;
import com.example.userservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections; // Import Collections

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        // Kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new UsernameNotFoundException("Account is not active. Status: " + account.getStatus());
        }

        // Tạo GrantedAuthority từ vai trò của Account
        // Spring Security yêu cầu vai trò bắt đầu bằng "ROLE_"
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + account.getRole().getName());

        return new User(
                account.getUsername(), // Sử dụng username làm principal
                account.getPasswordHash(),
                Collections.singleton(authority) // Chỉ có một vai trò
        );
    }
}