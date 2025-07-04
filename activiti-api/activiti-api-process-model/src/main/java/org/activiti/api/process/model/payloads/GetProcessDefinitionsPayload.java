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
package org.activiti.api.process.model.payloads;

import java.util.Set;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class GetProcessDefinitionsPayload implements Payload {

    private String id;
    private String processDefinitionId;
    private Set<String> processDefinitionKeys;
    private String processCategoryToExclude;

    public GetProcessDefinitionsPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public GetProcessDefinitionsPayload(String processDefinitionId,
                                        Set<String> processDefinitionKeys) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKeys = processDefinitionKeys;
    }

    public GetProcessDefinitionsPayload(String processDefinitionId,
                                        Set<String> processDefinitionKeys,
                                        String processCategoryToExclude) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKeys = processDefinitionKeys;
        this.processCategoryToExclude = processCategoryToExclude;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public boolean hasDefinitionKeys() {
        return processDefinitionKeys != null && !processDefinitionKeys.isEmpty();
    }

    public void setProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
    }

    public String getProcessCategoryToExclude() {
        return processCategoryToExclude;
    }
}
