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

package org.activiti.spring.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.activiti.spring.cache.ActivitiSpringCacheManagerProperties;
import org.activiti.spring.cache.caffeine.ActivitiSpringCaffeineCacheConfigurer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration(before = { CacheAutoConfiguration.class })
@EnableCaching
@EnableConfigurationProperties({ActivitiSpringCacheManagerProperties.class})
@PropertySource("classpath:config/activiti-spring-cache-manager.properties")
public class ActivitiSpringCacheManagerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "activiti.spring.cache-manager.provider", havingValue = "noop")
    public InitializingBean activitiSpringNoopCacheManagerInitializer(
        ActivitiSpringCacheManagerProperties properties,
        CacheManager cacheManager
    ) {
        return () -> {
            properties.getCaches()
                .entrySet()
                .stream()
                .filter(it -> it.getValue().isEnabled())
                .map(Map.Entry::getKey)
                .forEach(cacheManager::getCache);
        };
    }

    @Bean
    @ConditionalOnProperty(value = "activiti.spring.cache-manager.provider", havingValue = "simple")
    public CacheManagerCustomizer<ConcurrentMapCacheManager> activitiSpringSimpleCacheManagerCustomizer(ActivitiSpringCacheManagerProperties properties) {
        return cacheManager -> {
            List<String> cacheNames = new ArrayList<>();

            var cacheProperties = properties.getSimple();

            cacheManager.setAllowNullValues(cacheProperties.isAllowNullValues());

            properties.getCaches()
                .entrySet()
                .stream()
                .filter(it -> it.getValue().isEnabled())
                .map(Map.Entry::getKey)
                .forEach(cacheNames::add);

            cacheManager.setCacheNames(cacheNames);
        };
    }

    @Bean
    @ConditionalOnClass(CaffeineCacheManager.class)
    @ConditionalOnProperty(value = "activiti.spring.cache-manager.provider", havingValue = "caffeine")
    public CacheManagerCustomizer<CaffeineCacheManager> activitiSpringCaffeineCacheManagerCustomizer(
        ActivitiSpringCacheManagerProperties properties,
        ObjectProvider<ActivitiSpringCaffeineCacheConfigurer> cacheConfigurers
    ) {
        return cacheManager -> {
            var caffeineCacheProperties = properties.getCaffeine();

            cacheManager.setCaffeineSpec(CaffeineSpec.parse(caffeineCacheProperties.getDefaultSpec()));
            cacheManager.setAllowNullValues(caffeineCacheProperties.isAllowNullValues());

            properties.getCaches()
                .entrySet()
                .stream()
                .filter(it -> it.getValue().isEnabled())
                .forEach(cacheEntry -> {
                    Optional.ofNullable(cacheEntry.getValue())
                        .map(ActivitiSpringCacheManagerProperties.ActivitiCacheProperties::getCaffeine)
                        .map(CacheProperties.Caffeine::getSpec)
                        .or(() -> Optional.ofNullable(properties.getCaffeine().getDefaultSpec()))
                        .map(CaffeineSpec::parse)
                        .map(Caffeine::from)
                        .ifPresent(caffeine -> {

                            if (caffeineCacheProperties.isUseSystemScheduler()) {
                                caffeine.scheduler(Scheduler.systemScheduler());
                            }

                            var cache = cacheConfigurers
                                .orderedStream()
                                .filter(configurer -> configurer.test(cacheEntry.getKey()))
                                .findFirst()
                                .map(configurer -> configurer.apply(caffeine))
                                .orElseGet(caffeine::build);

                            cacheManager.registerCustomCache(cacheEntry.getKey(), cache);
                        });
                });
        };
    }

}
