package com.immortals.miniurl.service;

import com.immortals.miniurl.model.enums.UserTypes;
import com.immortals.miniurl.model.UrlMapping;
import com.immortals.miniurl.repository.UrlMappingRepository;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import com.immortals.miniurl.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpiryCheckerService {

    private final UrlMappingRepository urlMappingRepository;

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void expireItems() {
        try {
            List<UrlMapping> expiredItems = urlMappingRepository.findByExpiresAtBeforeAndIsActiveTrue(DateTimeUtils.nowInstant());

            for (UrlMapping item : expiredItems) {
                item.setIsActive(Boolean.FALSE);
                item.setUpdatedDate(DateTimeUtils.now());
                item.setUpdatedBy(UserTypes.SYSTEM.name());
            }


            urlMappingRepository.saveAllAndFlush(expiredItems);

            log.info("Expired items marked inactive: {}", expiredItems.size());
        } catch (Exception e) {
            throw new UrlShorteningException(e.getMessage(), e);
        }
    }
}
