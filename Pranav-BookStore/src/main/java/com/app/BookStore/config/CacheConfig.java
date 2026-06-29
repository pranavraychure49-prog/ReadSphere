package com.app.BookStore.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine as the backing store.
 *
 * <p><b>Caches defined:</b>
 * <ul>
 *   <li>{@code books}         – all books list</li>
 *   <li>{@code booksByCategory} – books filtered by category</li>
 *   <li>{@code booksByPublisher} – books filtered by publisher</li>
 * </ul>
 *
 * <p>Each cache entry expires 10 minutes after last write and holds at most
 * 500 entries. Cache is automatically evicted on any write, update, or delete.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_BOOKS             = "books";
    public static final String CACHE_BY_CATEGORY       = "booksByCategory";
    public static final String CACHE_BY_PUBLISHER      = "booksByPublisher";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                CACHE_BOOKS, CACHE_BY_CATEGORY, CACHE_BY_PUBLISHER
        );
        manager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)   // auto-expire after 10 min
                .maximumSize(500)                          // max 500 entries per cache
                .recordStats()                             // visible in /actuator/metrics
        );
        return manager;
    }
}
