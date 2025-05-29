package com.immortals.miniurl.repository;

import com.immortals.miniurl.model.UrlMapping;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<UrlMapping> findByShortUrlAndIsActiveTrue(String shortUrl);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<UrlMapping> findByShortUrlAndIsActiveTrueAndUserId(String shortUrl,Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UrlMapping> findByExpiresAtBeforeAndIsActiveTrue(Instant now);
}
