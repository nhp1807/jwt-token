package com.example.security.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccessTokenCache {
    private final LoadingCache<String, String> accessTokenCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    return null;
                }
            });

    public void put(String email, String accessToken) {
        accessTokenCache.put(email, accessToken);
    }

    public String get(String email) {
        return accessTokenCache.getUnchecked(email);
    }

    public void invalidate(String email) {
        accessTokenCache.invalidate(email);
        log.info("Invalidated access token for user ID: {}", email);
    }

    public void invalidateAll() {
        accessTokenCache.invalidateAll();
    }
}
