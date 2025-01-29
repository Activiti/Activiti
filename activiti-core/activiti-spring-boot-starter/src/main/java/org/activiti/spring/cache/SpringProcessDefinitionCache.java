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

import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.springframework.cache.Cache;

public class SpringProcessDefinitionCache implements DeploymentCache<ProcessDefinitionCacheEntry> {

    private final Cache delegate;

    public SpringProcessDefinitionCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public ProcessDefinitionCacheEntry get(String id) {
        return delegate.get(id, ProcessDefinitionCacheEntry.class);
    }

    @Override
    public void add(String id, ProcessDefinitionCacheEntry object) {
        delegate.putIfAbsent(id, object);
    }

    @Override
    public void remove(String id) {
        delegate.evictIfPresent(id);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean contains(String id) {
        return delegate.get(id) != null;
    }

    public Cache getDelegate() {
        return delegate;
    }

}
