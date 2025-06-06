package com.example.userservice.config;

import com.example.userservice.security.CustomUserDetailsService; // Import CustomUserDetailsService
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Cho phép @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Sử dụng BCrypt
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity // Kích hoạt Spring Security
@EnableMethodSecurity(prePostEnabled = true) // Cho phép @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter; // Sẽ inject filter JWT tự tạo

    // 1. Cấu hình PasswordEncoder (BCrypt là thuật toán được khuyến nghị)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Cấu hình AuthenticationProvider
    // Sử dụng DaoAuthenticationProvider để lấy UserDetails từ CustomUserDetailsService
    // và so sánh mật khẩu bằng PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Cung cấp CustomUserDetailsService của bạn
        authProvider.setPasswordEncoder(passwordEncoder()); // Cung cấp PasswordEncoder
        return authProvider;
    }

    // 3. Cấu hình AuthenticationManager
    // Được sử dụng bởi AuthService để xác thực người dùng (ví dụ: trong /auth/login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. Cấu hình SecurityFilterChain
    // Định nghĩa các quy tắc bảo mật cho HTTP requests
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì sử dụng JWT (stateless)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt CORS với cấu hình cụ thể
                .authorizeHttpRequests(auth -> auth
                        // Cho phép các endpoint xác thực/đăng ký/kiểm tra public mà không cần xác thực
                        .requestMatchers("/api/v1/auth/**", "/api/v1/accounts/check-username", "/api/v1/accounts/check-email").permitAll()
                        // Cho phép truy cập Swagger UI và API docs
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // Tất cả các request khác yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Vô hiệu hóa session (stateless API với JWT)
                )
                .authenticationProvider(authenticationProvider()) // Đăng ký AuthenticationProvider
                // Thêm JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
                // để JWT được xử lý trước khi Spring Security cố gắng xác thực bằng username/password từ request
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 5. Cấu hình CORS
    // Rất quan trọng khi frontend và backend chạy trên các domain/port khác nhau
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://your-frontend-domain.com")); // Thay thế bằng domain frontend của bạn
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*")); // Cho phép tất cả các headers
        configuration.setAllowCredentials(true); // Cho phép gửi cookies (nếu có, ví dụ refresh token trong cookie)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cho tất cả các đường dẫn
        return source;
    }
}