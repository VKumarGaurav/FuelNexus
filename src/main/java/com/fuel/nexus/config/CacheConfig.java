package com.fuel.nexus.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // By default, Spring Boot auto-configures cache manager (EhCache, Redis, etc.)
    // Additional cache beans can be defined if needed.
}
