/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.cache.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.ArrayList;
import java.util.List;
import org.activiti.spring.cache.caffeine.ActivitiSpringCaffeineCacheConfigurer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
    properties = {
        "debug=false",
        "activiti.spring.cache-manager.provider=caffeine",
        "activiti.spring.cache-manager.caffeine.allow-null-values=true",
        "activiti.spring.cache-manager.caches.foo.caffeine.spec=initialCapacity=100, maximumSize=1000, expireAfterAccess=60s, recordStats",
        "activiti.spring.cache-manager.caches.bar.enabled=false",
        "activiti.spring.cache-manager.caches.bar.caffeine.spec=initialCapacity=100, maximumSize=1000, expireAfterAccess=60s, recordStats",
})
class ActivitiSpringCaffeineCacheManagerTests {

    private static final List<Caffeine<Object,Object>> caffeineCacheConfigurers = new ArrayList<>();

    @SpringBootApplication
    static class TestApplication {

        @Bean
        ActivitiSpringCaffeineCacheConfigurer fooConfigurer() {
            return new ActivitiSpringCaffeineCacheConfigurer() {
                @Override
                public Cache<Object, Object> apply(Caffeine<Object, Object> caffeine) {
                    caffeineCacheConfigurers.add(caffeine);

                    return caffeine.build(key -> "bar");
                }

                @Override
                public boolean test(String cacheName) {
                    return "foo".equals(cacheName);
                }
            };
        }
    }

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Value("${spring.cache.type}")
    private CacheType springCacheType;

    @Test
    void testCacheManager() {
        assertThat(cacheManager)
            .isNotNull()
            .isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    void testAllowNullValues() {
        assertThat(CaffeineCacheManager.class.cast(cacheManager).isAllowNullValues()).isTrue();
    }

    @Test
    void testCaffeineCacheManager() {
        assertThat(cacheManager.getCacheNames()).containsExactly("foo");
    }

    @Test
    void testCaffeineCaches() {
        var cache = cacheManager.getCache("foo");

        assertThat(cache).isInstanceOf(CaffeineCache.class);
    }

    @Test
    void springCacheType() {
        assertThat(springCacheType).isEqualTo(CacheType.CAFFEINE);
    }

    @Test
    void activitiSpringCaffeineCacheConfigurer() {
        assertThat(caffeineCacheConfigurers).hasSize(1);

        var cache = cacheManager.getCache("foo");

        assertThat(cache.get("foo", String.class)).isEqualTo("bar");

    }

}
