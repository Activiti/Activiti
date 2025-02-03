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

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

import java.util.HashMap;
import java.util.Map;
import org.activiti.spring.cache.ActivitiSpringCacheManagerProperties.CacheProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class ActivitiSpringCacheManagerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    protected static final String ACTIVITI_SPRING_CACHE_MANAGER_PROVIDER_KEY = "activiti.spring.cache-manager.provider";
    protected static final String SPRING_CACHE_TYPE_KEY = "spring.cache.type";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final CacheProvider cacheProvider = environment.getProperty(
            ACTIVITI_SPRING_CACHE_MANAGER_PROVIDER_KEY,
            CacheProvider.class,
            CacheProvider.caffeine
        );

        environment
            .getPropertySources()
            .addAfter(
                SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MapPropertySource(this.getClass().getSimpleName(), resolvePropertiesToSet(cacheProvider))
            );
    }

    private Map<String, Object> resolvePropertiesToSet(CacheProvider messagingBroker) {
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put(SPRING_CACHE_TYPE_KEY, resolveCacheType(messagingBroker));

        return extraProperties;
    }

    private CacheType resolveCacheType(CacheProvider cacheProvider) {
        return switch (cacheProvider) {
            case simple -> CacheType.SIMPLE;
            case caffeine -> CacheType.CAFFEINE;
            default -> CacheType.NONE;
        };
    }
}
