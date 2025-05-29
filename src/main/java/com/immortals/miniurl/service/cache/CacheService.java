package com.immortals.miniurl.service.cache;

import java.time.Duration;
import java.util.Map;

public interface CacheService<K, V> {
    void put(K key, V value, Duration ttl);

    Boolean putIfAbsent(K key, V value, Duration ttl);

    void putMultipleIfAbsent(Map<K, V> entries, Duration ttl);

    V get(K key);

    void remove(K key);

    void clear();

    boolean containsKey(K key);

    default Long getHitCount() {
        return 0L;
    }

    default Long getMissCount() {
        return 0L;
    }


}
