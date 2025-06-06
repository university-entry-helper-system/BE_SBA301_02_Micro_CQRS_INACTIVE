package com.example.userservice.service.impl;

import com.example.userservice.entity.Account;
import com.example.userservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey; // Import SecretKey

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration-ms}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpiration;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // FIX 1: Change Jwts.parserBuilder() to Jwts.parser()
        // FIX 2: Change setSigningKey() to verifyWith() for parsing
        // FIX 3: Change parseClaimsJws() to parseSignedClaims()
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey()) // Casting to SecretKey is safe here
                .build()
                .parseSignedClaims(token) // New method for parsing signed JWTs
                .getPayload(); // Get the claims payload
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                // Keep signWith(Key, SignatureAlgorithm) for signing, as it's still valid
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateAccessToken(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountId", account.getId().toString());
        claims.put("roleId", account.getRole().getId());
        claims.put("roleName", account.getRole().getName());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(account.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountId", account.getId().toString());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(account.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public UUID extractAccountId(String token) {
        String accountIdStr = extractClaim(token, claims -> claims.get("accountId", String.class));
        return accountIdStr != null ? UUID.fromString(accountIdStr) : null;
    }

    @Override
    public Long extractRoleId(String token) {
        return extractClaim(token, claims -> claims.get("roleId", Long.class));
    }
}