package com.immortals.miniurl.service;

import com.immortals.miniurl.annotation.ReadOnly;
import com.immortals.miniurl.annotation.WriteOnly;
import com.immortals.miniurl.context.RequestContext;
import com.immortals.miniurl.context.StrategyContext;
import com.immortals.miniurl.factory.SmartUrlStrategySelectorFactory;
import com.immortals.miniurl.factory.UrlShorteningStrategyFactory;
import com.immortals.miniurl.model.domain.UrlMapping;
import com.immortals.miniurl.model.dto.CachedUrlDataDto;
import com.immortals.miniurl.model.dto.MiniUrlRequestDto;
import com.immortals.miniurl.model.dto.MiniUrlResponseDto;
import com.immortals.miniurl.model.enums.RedirectType;
import com.immortals.miniurl.model.enums.UrlStrategyType;
import com.immortals.miniurl.model.enums.UserTypes;
import com.immortals.miniurl.model.security.CurrentUserProvider;
import com.immortals.miniurl.repository.UrlMappingRepository;
import com.immortals.miniurl.service.cache.CacheService;
import com.immortals.miniurl.service.exception.CacheException;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import com.immortals.miniurl.utils.DateTimeUtils;
import com.immortals.miniurl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.immortals.miniurl.utils.UrlUtil.buildFullUrl;


@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;
    private final UrlShorteningStrategyFactory urlShorteningStrategyFactory;
    private final CurrentUserProvider currentUserProvider;
    private final CacheService<String, String> cacheService;

    AtomicLong hitUrlCount = new AtomicLong(0);

    @Value("${server.address}")
    private String address;

    @Value("${server.port}")
    private Long port;


    @WriteOnly
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public MiniUrlResponseDto createShortUrl(MiniUrlRequestDto miniUrlRequestDto) {
        log.info("Creating short URL for: {}", miniUrlRequestDto.getOriginalUrl());

        try {
            if (!StringUtils.hasText(miniUrlRequestDto.getOriginalUrl())) {
                throw new UrlShorteningException("Original URL must not be null or empty");
            }

            StrategyContext strategyContext = StrategyContext.builder()
                    .customAlias(miniUrlRequestDto.getCustomAlias())
                    .highThroughput(miniUrlRequestDto.getHighThroughput())
                    .internalTool(miniUrlRequestDto.getInternalTool())
                    .needsDeterminism(miniUrlRequestDto.getNeedsDeterminism())
                    .premiumUser(miniUrlRequestDto.getPremiumUser())
                    .useTimestamp(miniUrlRequestDto.getUseTimestamp())
                    .build();

            UrlStrategyType strategy = SmartUrlStrategySelectorFactory.selectStrategy(strategyContext);
            log.trace("Selected shortening strategy: {}", strategy.name());

            String shortUrl = urlShorteningStrategyFactory.getStrategy(strategy)
                    .generate(miniUrlRequestDto.getOriginalUrl());


            String finalShortUrl = buildFullUrl(address + ":" + port, shortUrl);


            log.trace("Generated short URL: {}", finalShortUrl);

            String existingUrl = getShortUrlIfExists(finalShortUrl, new AtomicReference<>(), currentUserProvider.getCurrentUser()
                    .getId());
            if (existingUrl != null && existingUrl.equals(miniUrlRequestDto.getOriginalUrl())) {
                log.info("Short URL already exists for given original URL: {}", finalShortUrl);
                return MiniUrlResponseDto.builder()
                        .shortUrl("Short URL already exists: " + finalShortUrl)
                        .build();
            }

            Instant expiryTime = DateTimeUtils.calculateExpiry(miniUrlRequestDto.getAmountOfTime(), miniUrlRequestDto.getUnitTime());
            UrlMapping urlMapping = UrlMapping.builder()
                    .originalUrl(miniUrlRequestDto.getOriginalUrl())
                    .shortUrl(finalShortUrl)
                    .userId(currentUserProvider.getCurrentUser()
                            .getId() != null ? currentUserProvider.getCurrentUser()
                            .getId() : 0L)
                    .numberOfClicks(hitUrlCount.get())
                    .notes(miniUrlRequestDto.getNote())
                    .customAliasName(miniUrlRequestDto.getCustomAliasName())
                    .customAlias(miniUrlRequestDto.getCustomAlias())
                    .premiumUser(miniUrlRequestDto.getPremiumUser())
                    .highThroughput(miniUrlRequestDto.getHighThroughput())
                    .needsDeterminism(miniUrlRequestDto.getNeedsDeterminism())
                    .internalTool(miniUrlRequestDto.getInternalTool())
                    .expiresAt(expiryTime)
                    .tags(JsonUtils.toJson(miniUrlRequestDto.getTags()))
                    .redirectType(RedirectType.TEMPORARY)
                    .strategy(strategy.name())
                    .createdUserAgent(RequestContext.getUserAgent())
                    .createdBy(UserTypes.SYSTEM.name())
                    .createdDate(DateTimeUtils.now())
                    .isActive(Boolean.TRUE)
                    .build();

            UrlMapping savedMapping = urlMappingRepository.saveAndFlush(urlMapping);
            log.info("Short URL saved to DB: {}", finalShortUrl);

            cacheService.put(finalShortUrl, JsonUtils.toJson(new CachedUrlDataDto(savedMapping.getOriginalUrl(), savedMapping.getExpiresAt())), DateTimeUtils.durationBetween(Instant.now(), savedMapping.getExpiresAt()));

            log.info("Short URL successfully cached: {}", finalShortUrl);
            return MiniUrlResponseDto.builder()
                    .shortUrl(finalShortUrl)
                    .build();

        } catch (DataAccessException | URISyntaxException | CacheException e) {
            log.error("Error occurred while creating short URL: {}", e.getMessage(), e);
            throw new UrlShorteningException(e.getMessage(), e);
        }
    }

    @ReadOnly
    @Override
    public String getLongUrl(String shortUrl) {
        log.trace("Resolving original URL for short URL: {}", shortUrl);

        Object cachedData = cacheService.get(shortUrl);
        if (cachedData != null) {
            incrementUrlAndUpdateTable(shortUrl, hitUrlCount.incrementAndGet());
            CachedUrlDataDto cachedUrlDataDto = JsonUtils.fromJson(cachedData.toString(), CachedUrlDataDto.class);
            log.trace("Cache hit for short URL: {}", shortUrl);
            return cachedUrlDataDto.getOriginalUrl();
        }

        log.trace("Cache miss for short URL: {}", shortUrl);

        AtomicReference<String> originalUrl = new AtomicReference<>();

        incrementUrlAndUpdateTable(shortUrl, hitUrlCount.incrementAndGet());
        getUrlIfNotFoundInCache(shortUrl, originalUrl);
        return originalUrl.get();
    }

    @ReadOnly
    private void getUrlIfNotFoundInCache(String shortUrl, AtomicReference<String> originalUrl) {
        urlMappingRepository.findByShortUrlAndIsActiveTrue(shortUrl)
                .ifPresentOrElse(mapping -> {
                    log.trace("Found original URL in DB for short URL: {}", shortUrl);
                    originalUrl.set(mapping.getOriginalUrl());
                }, () -> log.warn("No mapping found in DB for short URL: {}", shortUrl));
    }

    @WriteOnly
    @Async
    public void incrementUrlAndUpdateTable(String shortUrl, long urlHitCount) {
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrlAndIsActiveTrue(shortUrl);
        urlMapping.ifPresent(mapping -> {
            mapping.setNumberOfClicks(urlHitCount);
            urlMappingRepository.saveAndFlush(mapping);
        });
    }

    @ReadOnly
    private String getShortUrlIfExists(String shortUrl, AtomicReference<String> originalUrl, Long userId) {
        urlMappingRepository.findByShortUrlAndIsActiveTrueAndUserId(shortUrl, userId)
                .ifPresentOrElse(mapping -> {
                    log.trace("Found original URL in DB for short URL with User Id: {} {}", shortUrl, userId);
                    originalUrl.set(mapping.getOriginalUrl());
                }, () -> log.warn("No mapping found in DB for short URL with User Id: {} {}", shortUrl, userId));
        return originalUrl.get();
    }
}