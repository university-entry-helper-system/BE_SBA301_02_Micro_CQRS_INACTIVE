package com.example.userservice.service.impl;

import com.example.userservice.dto.request.AccountCreationRequest;
import com.example.userservice.dto.request.LoginRequest;
import com.example.userservice.dto.request.RefreshTokenRequest;
import com.example.userservice.dto.response.AuthResponse;
import com.example.userservice.entity.Account;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.Token;
import com.example.userservice.repository.AccountRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.TokenRepository;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.JwtService;
import com.example.userservice.service.MailService;
import com.example.userservice.util.RoleConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager; // Cần cho luồng authenticate

    @Value("${application.client.base-url}")
    private String clientBaseUrl;
    @Value("${application.client.email-verification-path}")
    private String emailVerificationPath;
    @Value("${application.client.password-reset-path}")
    private String passwordResetPath;

    @Override
    @Transactional
    public void registerUser(AccountCreationRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists."); // Hoặc ném ngoại lệ cụ thể hơn
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists."); // Hoặc ném ngoại lệ cụ thể hơn
        }

        Role defaultRole = roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(() -> new RuntimeException("Default USER role not found."));

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newAccount.setFullName(request.getFullName());
        newAccount.setPhone(request.getPhone());
        newAccount.setRole(defaultRole);
        newAccount.setStatus("PENDING_VERIFICATION"); // Đặt trạng thái chờ xác minh

        Account savedAccount = accountRepository.save(newAccount);

        // Tạo activation code và gửi email
        // Logic tạo code có thể phức tạp hơn (ví dụ: lưu vào DB với thời hạn)
        String activationCode = UUID.randomUUID().toString(); // Dùng UUID tạm thời làm code
        // Trong thực tế, bạn sẽ lưu activationCode này vào DB, gắn với account_id và thời gian hết hạn
        // Ví dụ: new AccountActivationToken(savedAccount.getId(), activationCode, Instant.now().plus(24, ChronoUnit.HOURS))
        // và lưu vào một bảng riêng.

        String activationLink = String.format("%s%s?email=%s&code=%s",
                clientBaseUrl, emailVerificationPath, savedAccount.getEmail(), activationCode);
        mailService.sendActivationEmail(savedAccount.getEmail(), activationLink);

        log.info("Registered user: {} and sent activation email.", savedAccount.getUsername());
    }

    @Override
    @Transactional
    public void activateAccount(String email, String code) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        if (!"PENDING_VERIFICATION".equals(account.getStatus())) {
            throw new RuntimeException("Account is not in pending verification status.");
        }

        // Trong thực tế, bạn sẽ kiểm tra 'code' với mã đã lưu trong DB cho tài khoản này
        // (ví dụ: kiểm tra bảng AccountActivationToken)
        // Nếu code hợp lệ và chưa hết hạn:
        // if (isValidActivationCode(email, code)) {
        account.setStatus("ACTIVE");
        accountRepository.save(account);
        log.info("Account {} activated successfully.", account.getUsername());
        // Sau khi sử dụng, xóa hoặc đánh dấu code là đã dùng trong DB
        // } else {
        //    throw new RuntimeException("Invalid or expired activation code.");
        // }
    }

    @Override
    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        // Spring Security sẽ xử lý xác thực thông qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        // Lấy thông tin account sau khi xác thực thành công
        Account account = accountRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication. This should not happen."));

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active. Please activate your account.");
        }

        // Revoke all existing valid tokens for this user upon new login (optional, but good for security)
        tokenRepository.revokeAllTokensByAccountId(account.getId());

        // Generate new Access Token
        String accessToken = jwtService.generateAccessToken(account);

        // Generate new Refresh Token and save it
        String refreshToken = jwtService.generateRefreshToken(account);
        Token newToken = Token.builder()
                .tokenValue(refreshToken)
                .account(account)
                .userAgent("login-web") // Có thể lấy từ HttpServletRequest
                .ipAddress("127.0.0.1") // Có thể lấy từ HttpServletRequest
                .expiresAt(Instant.now().plusSeconds(jwtService.extractExpiration(refreshToken).getTime() / 1000))
                .revoked(false)
                .build();
        tokenRepository.save(newToken);

        return new AuthResponse(accessToken, refreshToken, jwtService.extractExpiration(accessToken).getTime());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        Token storedToken = tokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found or invalid."));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token is revoked or expired.");
        }

        Account account = storedToken.getAccount();

        // Generate new Access Token
        String newAccessToken = jwtService.generateAccessToken(account);

        // Optionally, generate a new refresh token and revoke the old one for better security (token rotation)
        tokenRepository.save(storedToken.toBuilder().revoked(true).build()); // Revoke old token
        String newRefreshTokenValue = jwtService.generateRefreshToken(account);
        Token newToken = Token.builder()
                .tokenValue(newRefreshTokenValue)
                .account(account)
                .userAgent(storedToken.getUserAgent())
                .ipAddress(storedToken.getIpAddress())
                .expiresAt(Instant.now().plusSeconds(jwtService.extractExpiration(newRefreshTokenValue).getTime() / 1000))
                .revoked(false)
                .build();
        tokenRepository.save(newToken);


        return new AuthResponse(newAccessToken, newRefreshTokenValue, jwtService.extractExpiration(newAccessToken).getTime());
    }


    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found."));

        // Tạo reset token (có thể lưu vào DB, với thời hạn)
        String resetToken = UUID.randomUUID().toString();
        // Ví dụ: Lưu vào bảng Token, loại token "PASSWORD_RESET" với expires_at ngắn
        // tokenRepository.save(new Token(account, resetToken, "PASSWORD_RESET", Instant.now().plus(1, ChronoUnit.HOURS), false));

        String resetLink = String.format("%s%s?email=%s&token=%s",
                clientBaseUrl, passwordResetPath, account.getEmail(), resetToken);
        mailService.sendPasswordResetEmail(account.getEmail(), resetLink);

        log.info("Password reset link sent to: {}", account.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(String email, String token, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        // Trong thực tế, bạn sẽ xác thực 'token' với mã đã lưu trong DB
        // (ví dụ: kiểm tra bảng Token với loại "PASSWORD_RESET" và đảm bảo chưa hết hạn)
        // Nếu token hợp lệ:
        // if (isValidResetToken(email, token)) {
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        log.info("Password for account {} reset successfully.", account.getUsername());
        // Sau khi sử dụng, xóa hoặc đánh dấu token là đã dùng trong DB
        // } else {
        //    throw new RuntimeException("Invalid or expired password reset token.");
        // }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        Token storedToken = tokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found.")); // Token không tồn tại thì cũng là logout

        if (!storedToken.isRevoked()) {
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            log.info("Refresh token for account {} revoked.", storedToken.getAccount().getUsername());
        }
    }

    @Override
    public boolean existByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }
}