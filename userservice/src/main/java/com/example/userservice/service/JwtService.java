package com.example.userservice.service;

import com.example.userservice.entity.Account;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    Date extractExpiration(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateToken(UserDetails userDetails);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    String generateAccessToken(Account account);
    String generateRefreshToken(Account account);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
    UUID extractAccountId(String token); // Extract accountId from token
    Long extractRoleId(String token); // Extract roleId from token (if included in claims)
}