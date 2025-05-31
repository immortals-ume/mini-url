package com.immortals.miniurl.service;

import com.immortals.miniurl.annotation.WriteOnly;
import com.immortals.miniurl.model.domain.UrlMapping;
import com.immortals.miniurl.model.enums.UserTypes;
import com.immortals.miniurl.repository.UrlMappingRepository;
import com.immortals.miniurl.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpiryCheckerService {

    private final UrlMappingRepository urlMappingRepository;

    private final ReentrantLock executionLock = new ReentrantLock();

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    @WriteOnly
    public void runScheduledExpiryJob() {
        if (!executionLock.tryLock()) {
            log.warn("Skipping ExpiryChecker execution: previous job is still running.");
            return;
        }

        try {
            Instant now = DateTimeUtils.nowInstant();
            List<UrlMapping> expiredItems = urlMappingRepository.findByExpiresAtBeforeAndIsActiveTrue(now);

            if (expiredItems == null || expiredItems.isEmpty()) {
                log.info("No expired URL mappings found at {}", now);
                return;
            }

            for (UrlMapping item : expiredItems) {
                item.setIsActive(Boolean.FALSE);
                item.setUpdatedDate(DateTimeUtils.now());
                item.setUpdatedBy(UserTypes.SYSTEM.name());
            }

            urlMappingRepository.saveAllAndFlush(expiredItems);

            log.info("Expired {} URL mappings at {}", expiredItems.size(), now);
        } catch (Exception e) {
            log.error("Expiry job failed: {}", e.getMessage(), e);
        } finally {
            executionLock.unlock();
        }
    }

}
