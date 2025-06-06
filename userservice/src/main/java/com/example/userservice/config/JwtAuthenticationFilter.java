package com.example.userservice.config;

import com.example.userservice.security.CustomUserDetailsService;
import com.example.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService; // Để tải UserDetails

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Kiểm tra Header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Không có JWT, tiếp tục chuỗi filter
        }

        jwt = authHeader.substring(7); // Lấy phần token sau "Bearer "

        try {
            username = jwtService.extractUsername(jwt); // Lấy username từ JWT

            // 2. Kiểm tra nếu username hợp lệ và chưa có Authentication trong SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 3. Xác thực Token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Tạo đối tượng Authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Không có credentials (mật khẩu) vì đã xác thực bằng token
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Đặt Authentication vào SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated user: {} with roles: {}", username, userDetails.getAuthorities());
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // Clear context in case of invalid token
            SecurityContextHolder.clearContext();
            // Optional: set HTTP status for invalid token, e.g., 401 Unauthorized
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // return;
        }

        // 4. Chuyển request tới filter tiếp theo trong chuỗi
        filterChain.doFilter(request, response);
    }
}