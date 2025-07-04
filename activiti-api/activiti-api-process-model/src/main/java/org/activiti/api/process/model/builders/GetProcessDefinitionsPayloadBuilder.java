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
package org.activiti.api.process.model.builders;

import java.util.HashSet;
import java.util.Set;

import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;

public class GetProcessDefinitionsPayloadBuilder {

    private String processDefinitionId;
    private Set<String> processDefinitionKeys = new HashSet<>();
    private String processCategoryToExclude;

    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
        return this;
    }

    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public GetProcessDefinitionsPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        if (processDefinitionKeys == null) {
            processDefinitionKeys = new HashSet<>();
        }
        processDefinitionKeys.add(processDefinitionKey);
        return this;
    }

    public GetProcessDefinitionsPayloadBuilder withProcessCategoryToExclude(String processCategoryToExclude) {
        this.processCategoryToExclude = processCategoryToExclude;
        return this;
    }

    public GetProcessDefinitionsPayload build() {
        return new GetProcessDefinitionsPayload(processDefinitionId, processDefinitionKeys, processCategoryToExclude);
    }
}
