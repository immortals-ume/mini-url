package com.immortals.miniurl.service.cache;

import com.immortals.miniurl.config.cache.CacheProperties;
import com.immortals.miniurl.service.exception.CacheException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private CacheProperties cacheProperties;

    @InjectMocks
    private RedisCacheService<String, String> redisCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        redisCacheService = new RedisCacheService<>(redisTemplate);
    }

    @Test
    void testPut_success() {
        doNothing().when(valueOps)
                .set("key", "value", Duration.ofSeconds(60));
        assertDoesNotThrow(() -> redisCacheService.put("key", "value", Duration.ofSeconds(60)));
    }

    @Test
    void testPut_failure_shouldThrow() {
        doThrow(new DataAccessException("fail") {
        }).when(valueOps)
                .set(any(), any(), any());
        CacheException ex = assertThrows(CacheException.class, () ->
                redisCacheService.put("key", "value", Duration.ofSeconds(60))
        );
        assertEquals("Failed to put value in cache", ex.getMessage());
    }

    @Test
    void testPutIfAbsent_success() {
        when(valueOps.setIfAbsent("key", "value", Duration.ofSeconds(30))).thenReturn(true);
        assertTrue(redisCacheService.putIfAbsent("key", "value", Duration.ofSeconds(30)));
        assertEquals(1, redisCacheService.getHitCount());
    }

    @Test
    void testPutIfAbsent_exists() {
        when(valueOps.setIfAbsent("key", "value", Duration.ofSeconds(30))).thenReturn(false);
        assertFalse(redisCacheService.putIfAbsent("key", "value", Duration.ofSeconds(30)));
        assertEquals(1, redisCacheService.getMissCount());
    }

    @Test
    void testPutIfAbsent_failure_shouldThrow() {
        when(valueOps.setIfAbsent(any(), any(), any()))
                .thenThrow(new DataAccessException("fail") {
                });
        assertThrows(CacheException.class, () ->
                redisCacheService.putIfAbsent("key", "value", Duration.ofSeconds(10))
        );
        assertEquals(1, redisCacheService.getMissCount());
    }

    @Test
    void testGet_hit() {
        when(valueOps.get("key")).thenReturn("value");
        assertEquals("value", redisCacheService.get("key"));
        assertEquals(1, redisCacheService.getHitCount());
    }

    @Test
    void testGet_miss() {
        when(valueOps.get("key")).thenReturn(null);
        assertNull(redisCacheService.get("key"));
        assertEquals(1, redisCacheService.getMissCount());
    }

    @Test
    void testGet_failure_shouldThrow() {
        when(valueOps.get("key")).thenThrow(new DataAccessException("fail") {
        });
        assertThrows(CacheException.class, () -> redisCacheService.get("key"));
    }


    @Test
    void testRemove_failure_shouldThrow() {
        doThrow(new DataAccessException("fail") {
        }).when(redisTemplate)
                .delete("key");
        assertThrows(CacheException.class, () -> redisCacheService.remove("key"));
    }

    @Test
    void testClear_success() {
        RedisConnection connection = mock(RedisConnection.class);
        RedisServerCommands serverCommands = mock(RedisServerCommands.class);

        when(connection.serverCommands()).thenReturn(serverCommands);
        doNothing().when(serverCommands)
                .flushAll(any());

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<Void> callback = invocation.getArgument(0);
            return callback.doInRedis(connection);
        });

        assertDoesNotThrow(() -> redisCacheService.clear());
    }

    @Test
    void testClear_failure_shouldThrow() {
        when(redisTemplate.execute(any(RedisCallback.class)))
                .thenThrow(new DataAccessException("fail") {
                });
        assertThrows(CacheException.class, () -> redisCacheService.clear());
    }

    @Test
    void testContainsKey_true() {
        when(redisTemplate.hasKey("key")).thenReturn(true);
        assertTrue(redisCacheService.containsKey("key"));
    }

    @Test
    void testContainsKey_false() {
        when(redisTemplate.hasKey("key")).thenReturn(false);
        assertFalse(redisCacheService.containsKey("key"));
    }

    @Test
    void testContainsKey_failure_shouldThrow() {
        when(redisTemplate.hasKey("key")).thenThrow(new DataAccessException("fail") {
        });
        assertThrows(CacheException.class, () -> redisCacheService.containsKey("key"));
    }

    @Test
    void testPutMultipleIfAbsent_success() {
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        RedisConnection connection = mock(RedisConnection.class);
        when(connection.exec()).then(inv -> null);

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(inv -> {
            RedisCallback<Void> cb = inv.getArgument(0);
            return cb.doInRedis(connection);
        });

        assertDoesNotThrow(() -> redisCacheService.putMultipleIfAbsent(map, Duration.ofSeconds(30)));
    }

    @Test
    void testPutMultipleIfAbsent_failure_shouldThrow() {
        when(redisTemplate.execute(any(RedisCallback.class)))
                .thenThrow(new DataAccessException("fail") {
                });
        assertThrows(CacheException.class, () ->
                redisCacheService.putMultipleIfAbsent(Map.of("a", "b"), Duration.ofSeconds(30))
        );
    }


    @Test
    void testShutdown_logsMetrics() {
        redisCacheService.putIfAbsent("a", "b", Duration.ofSeconds(10));
        redisCacheService.shutdown();
        // Just ensure no exception
    }
}
