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

package org.activiti.spring.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("activiti.spring.cache-manager")
public class ActivitiSpringCacheManagerProperties {

    public enum CacheProvider {
        noop,
        simple,
        caffeine,
    }

    private boolean enabled = true;

    private CacheProvider provider = CacheProvider.caffeine;

    private final Map<String, ActivitiCacheProperties> caches = new LinkedHashMap<>();

    private CaffeineCacheProviderProperties caffeine = new CaffeineCacheProviderProperties();

    private SimpleCacheProviderProperties simple = new SimpleCacheProviderProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CacheProvider getProvider() {
        return provider;
    }

    public void setProvider(CacheProvider provider) {
        this.provider = provider;
    }

    public Map<String, ActivitiCacheProperties> getCaches() {
        return caches;
    }

    public CaffeineCacheProviderProperties getCaffeine() {
        return caffeine;
    }

    public SimpleCacheProviderProperties getSimple() {
        return simple;
    }

    public void setCaffeine(CaffeineCacheProviderProperties caffeine) {
        this.caffeine = caffeine;
    }

    public void setSimple(SimpleCacheProviderProperties simple) {
        this.simple = simple;
    }

    public static class ActivitiCacheProperties {

        private boolean enabled = true;

        private CacheProperties.Caffeine caffeine = new CacheProperties.Caffeine();

        public CacheProperties.Caffeine getCaffeine() {
            return this.caffeine;
        }

        public void setCaffeine(CacheProperties.Caffeine caffeine) {
            this.caffeine = caffeine;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class CaffeineCacheProviderProperties {
        private boolean allowNullValues = true;

        private boolean useSystemScheduler = true;

        private String defaultSpec = "";

        public boolean isAllowNullValues() {
            return allowNullValues;
        }

        public String getDefaultSpec() {
            return defaultSpec;
        }

        public void setAllowNullValues(boolean allowNullValues) {
            this.allowNullValues = allowNullValues;
        }

        public void setDefaultSpec(String defaultSpec) {
            this.defaultSpec = defaultSpec;
        }

        public boolean isUseSystemScheduler() {
            return useSystemScheduler;
        }

        public void setUseSystemScheduler(boolean useSystemScheduler) {
            this.useSystemScheduler = useSystemScheduler;
        }
    }

    public static class SimpleCacheProviderProperties {
        private boolean allowNullValues = true;

        public boolean isAllowNullValues() {
            return allowNullValues;
        }

        public void setAllowNullValues(boolean allowNullValues) {
            this.allowNullValues = allowNullValues;
        }

    }

}
