package com.immortals.miniurl.service;

import com.immortals.miniurl.context.RequestContext;
import com.immortals.miniurl.context.StrategyContext;
import com.immortals.miniurl.model.dto.CachedUrlData;
import com.immortals.miniurl.model.dto.UrlShortenerDto;
import com.immortals.miniurl.model.enums.RedirectType;
import com.immortals.miniurl.model.enums.UrlStrategyType;
import com.immortals.miniurl.factory.SmartUrlStrategySelectorFactory;
import com.immortals.miniurl.factory.UrlShorteningStrategyFactory;
import com.immortals.miniurl.model.UrlAccessLog;
import com.immortals.miniurl.model.UrlMapping;
import com.immortals.miniurl.repository.UrlAccessLogRepository;
import com.immortals.miniurl.repository.UrlMappingRepository;
import com.immortals.miniurl.model.security.CurrentUserProvider;
import com.immortals.miniurl.service.cache.CacheService;
import com.immortals.miniurl.service.exception.CacheException;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import com.immortals.miniurl.utils.DateTimeUtils;
import com.immortals.miniurl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static com.immortals.miniurl.utils.UrlUtil.buildFullUrl;



@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final UrlAccessLogRepository urlAccessLogRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final UrlShorteningStrategyFactory urlShorteningStrategyFactory;
    private final CurrentUserProvider currentUserProvider;
    private final CacheService<String, String> cacheService;

    @Value("${server.address}")
    private String address;

    @Value("${server.port}")
    private Long port;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String createShortUrl(UrlShortenerDto urlShortenerDto) {
        log.info("Creating short URL for: {}", urlShortenerDto.getOriginalUrl());

        try {
            if (!StringUtils.hasText(urlShortenerDto.getOriginalUrl())) {
                throw new UrlShorteningException("Original URL must not be null or empty");
            }

            StrategyContext strategyContext = StrategyContext.builder()
                    .customAlias(urlShortenerDto.getCustomAlias())
                    .highThroughput(urlShortenerDto.getHighThroughput())
                    .internalTool(urlShortenerDto.getInternalTool())
                    .needsDeterminism(urlShortenerDto.getNeedsDeterminism())
                    .premiumUser(urlShortenerDto.getPremiumUser())
                    .build();

            UrlStrategyType strategy = SmartUrlStrategySelectorFactory.selectStrategy(strategyContext);
            log.trace("Selected shortening strategy: {}", strategy.name());

            String shortUrl = urlShorteningStrategyFactory.getStrategy(strategy)
                    .generate(urlShortenerDto.getOriginalUrl());


            String finalShortUrl = buildFullUrl(address + ":" + port, shortUrl);


            log.trace("Generated short URL: {}", finalShortUrl);



            String existingUrl = getShortUrlIfExists(shortUrl,new AtomicReference<>(), currentUserProvider.getCurrentUser().getId());
            if (existingUrl != null && existingUrl.equals(urlShortenerDto.getOriginalUrl())) {
                log.info("Short URL already exists for given original URL: {}", finalShortUrl);
                return "Short URL already exists: " + finalShortUrl;
            }

            Instant expiryTime = DateTimeUtils.calculateExpiry(urlShortenerDto.getAmount(), urlShortenerDto.getUnitTime());
            UrlMapping urlMapping = UrlMapping.builder()
                    .originalUrl(urlShortenerDto.getOriginalUrl())
                    .shortUrl(finalShortUrl)
                    .userId(1L)
                    .notes(urlShortenerDto.getNote())
                    .customAliasName(urlShortenerDto.getCustomAliasName())
                    .customAlias(urlShortenerDto.getCustomAlias())
                    .premiumUser(urlShortenerDto.getPremiumUser())
                    .highThroughput(urlShortenerDto.getHighThroughput())
                    .needsDeterminism(urlShortenerDto.getNeedsDeterminism())
                    .internalTool(urlShortenerDto.getInternalTool())
                    .expiresAt(expiryTime)
                    .tags(JsonUtils.toJson(urlShortenerDto.getTags()))
                    .redirectType(RedirectType.TEMPORARY)
                    .strategy(strategy.name())
                    .createdUserAgent(RequestContext.getUserAgent())
                    .isActive(Boolean.TRUE)
                    .build();

            UrlMapping savedMapping = urlMappingRepository.saveAndFlush(urlMapping);
            log.info("Short URL saved to DB: {}", finalShortUrl);

            boolean cacheSuccess = cacheService.putIfAbsent(finalShortUrl, JsonUtils.toJson(new CachedUrlData(savedMapping.getOriginalUrl(), savedMapping.getExpiresAt())), DateTimeUtils.durationBetween(Instant.now(), savedMapping.getExpiresAt()));

            if (!cacheSuccess) {
                log.error("Failed to cache short URL: {}", finalShortUrl);
                throw new CacheException("Some Issue Storing Data in Cache");
            }

            log.info("Short URL successfully cached: {}", finalShortUrl);
            return finalShortUrl;

        } catch (DataAccessException | URISyntaxException e) {
            log.error("Error occurred while creating short URL: {}", e.getMessage(), e);
            throw new UrlShorteningException(e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public String getLongUrl(String shortUrl) {
        log.trace("Resolving original URL for short URL: {}", shortUrl);

        Object cachedData = cacheService.get(shortUrl);
        if (cachedData != null) {

            CachedUrlData cachedUrlData = JsonUtils.fromJson(cachedData.toString(), CachedUrlData.class);
            log.trace("Cache hit for short URL: {}", shortUrl);
            saveUrlAccessData(shortUrl);
            return cachedUrlData.getOriginalUrl();
        }

        log.trace("Cache miss for short URL: {}", shortUrl);

        AtomicReference<String> originalUrl = new AtomicReference<>();

        urlMappingRepository.findByShortUrlAndIsActiveTrue(shortUrl)
                .ifPresentOrElse(mapping -> {
                    log.trace("Found original URL in DB for short URL: {}", shortUrl);
                    originalUrl.set(mapping.getOriginalUrl());
                }, () -> log.warn("No mapping found in DB for short URL: {}", shortUrl));
        saveUrlAccessData(shortUrl);
        return originalUrl.get();
    }

    private String getShortUrlIfExists(String shortUrl, AtomicReference<String> originalUrl,Long userId) {
        urlMappingRepository.findByShortUrlAndIsActiveTrueAndUserId(shortUrl,userId)
                .ifPresentOrElse(mapping -> {
                    log.trace("Found original URL in DB for short URL with User Id: {} {}", shortUrl,userId);
                    originalUrl.set(mapping.getOriginalUrl());
                }, () -> log.warn("No mapping found in DB for short URL with User Id: {} {}", shortUrl,userId));
        return originalUrl.get();
    }

    private void saveUrlAccessData(String shortUrl) {
        UrlAccessLog logEntry = UrlAccessLog.builder()
                .accessCount(1L)
                .accessedAt(OffsetDateTime.now())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .referer(1L)
                .deviceType("Desktop")
                .responseStatus((long) HttpStatus.TEMPORARY_REDIRECT.value())
                .responseTimeMs(150L)
                .build();

    }
}