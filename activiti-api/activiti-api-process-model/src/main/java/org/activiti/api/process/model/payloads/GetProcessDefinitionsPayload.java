/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.api.process.model.payloads;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.activiti.api.model.shared.Payload;

public class GetProcessDefinitionsPayload implements Payload {

    private String id;
    private Set<String> processDefinitionIds;
    private Set<String> processDefinitionKeys;
    private boolean latestVersionOnly;

    public GetProcessDefinitionsPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetProcessDefinitionsPayload(Set<String> processDefinitionIds, Set<String> processDefinitionKeys) {
        this();
        this.processDefinitionIds = processDefinitionIds;
        this.processDefinitionKeys = processDefinitionKeys;
    }

    public GetProcessDefinitionsPayload(
        Set<String> processDefinitionIds,
        Set<String> processDefinitionKeys,
        boolean latestVersionOnly
    ) {
        this();
        this.processDefinitionIds = processDefinitionIds;
        this.processDefinitionKeys = processDefinitionKeys;
        this.latestVersionOnly = latestVersionOnly;
    }

    public GetProcessDefinitionsPayload(String processDefinitionId, Set<String> processDefinitionKeys) {
        this(new HashSet<>(Set.of(processDefinitionId)), processDefinitionKeys);
    }

    public GetProcessDefinitionsPayload(
        String processDefinitionId,
        Set<String> processDefinitionKeys,
        boolean latestVersionOnly
    ) {
        this(new HashSet<>(Set.of(processDefinitionId)), processDefinitionKeys, latestVersionOnly);
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * @deprecated Use {@link #getProcessDefinitionIds()} instead
     */
    @Deprecated(since = "Use getProcessDefinitionIds() instead")
    public String getProcessDefinitionId() {
        if (processDefinitionIds == null) {
            return null;
        }
        return processDefinitionIds.stream().findFirst().orElse(null);
    }

    public Set<String> getProcessDefinitionIds() {
        return processDefinitionIds;
    }

    public void setProcessDefinitionIds(Set<String> processDefinitionIds) {
        this.processDefinitionIds = processDefinitionIds;
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public boolean hasDefinitionKeys() {
        return processDefinitionKeys != null && !processDefinitionKeys.isEmpty();
    }

    public boolean hasDefinitionIds() {
        return processDefinitionIds != null && !processDefinitionIds.isEmpty();
    }

    public void setProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
    }

    public boolean isLatestVersionOnly() {
        return latestVersionOnly;
    }

    public void setLatestVersionOnly(boolean latestVersionOnly) {
        this.latestVersionOnly = latestVersionOnly;
    }
}
