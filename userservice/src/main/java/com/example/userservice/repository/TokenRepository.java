package com.example.userservice.repository;

import com.example.userservice.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    // Tìm kiếm token dựa trên giá trị tokenValue
    Optional<Token> findByTokenValue(String tokenValue);

    // Tìm kiếm tất cả các token hợp lệ (chưa bị thu hồi và chưa hết hạn) của một account
    @Query("SELECT t FROM Token t WHERE t.account.id = :accountId AND t.revoked = false AND t.expiresAt > :now")
    List<Token> findValidTokensByAccountId(@Param("accountId") UUID accountId, @Param("now") Instant now);

    // Thu hồi tất cả các token (đặt revoked = true) cho một account
    @Modifying
    @Query("UPDATE Token t SET t.revoked = true WHERE t.account.id = :accountId")
    void revokeAllTokensByAccountId(@Param("accountId") UUID accountId);
}