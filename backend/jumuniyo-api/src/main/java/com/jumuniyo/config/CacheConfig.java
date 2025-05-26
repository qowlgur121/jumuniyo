package com.jumuniyo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

/**
 * 캐싱 설정 클래스
 * 개발 환경에서는 인메모리 캐시를 사용하고, 
 * 프로덕션 환경에서는 Redis 캐시를 사용합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // 캐시 이름 상수 정의
    public static final String STORE_CACHE = "storeCache";
    public static final String STORE_LIST_CACHE = "storeListCache";
    public static final String STORE_COUNT_CACHE = "storeCountCache";
    public static final String CATEGORY_CACHE = "categoryCache";
    
    /**
     * 개발 환경용 인메모리 캐시 매니저
     */
    @Bean
    @Profile({"default", "dev"})
    public CacheManager cacheManagerDev() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(STORE_CACHE),
                new ConcurrentMapCache(STORE_LIST_CACHE),
                new ConcurrentMapCache(STORE_COUNT_CACHE),
                new ConcurrentMapCache(CATEGORY_CACHE)
        ));
        return cacheManager;
    }
    
    /**
     * 프로덕션 환경용 캐시 매니저는 별도 구현 필요
     * (Redis 등을 사용하여 구현)
     */
} 