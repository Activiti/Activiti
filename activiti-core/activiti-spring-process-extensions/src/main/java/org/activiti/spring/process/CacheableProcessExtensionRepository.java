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


package org.activiti.spring.process;

import static org.activiti.spring.process.CacheableProcessExtensionRepository.PROCESS_EXTENSIONS_CACHE_NAME;

import java.util.Optional;
import org.activiti.spring.process.model.Extension;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;

@CacheConfig(cacheNames = {PROCESS_EXTENSIONS_CACHE_NAME})
public class CacheableProcessExtensionRepository implements ProcessExtensionRepository {

    public static final String PROCESS_EXTENSIONS_CACHE_NAME = "processExtensionsById";

    private final ProcessExtensionRepository delegate;

    public CacheableProcessExtensionRepository(ProcessExtensionRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable
    public Optional<Extension> getExtensionsForId(@NonNull String processDefinitionId) {
        return delegate.getExtensionsForId(processDefinitionId);
    }

}
