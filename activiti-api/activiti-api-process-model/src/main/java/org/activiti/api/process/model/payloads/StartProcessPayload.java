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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.activiti.api.model.shared.Payload;

public class StartProcessPayload implements Payload {

    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;
    private String businessKey;
    private Map<String, Object> variables = new HashMap<>();

    public StartProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public StartProcessPayload(
        String processDefinitionId,
        String processDefinitionKey,
        String name,
        String businessKey,
        Map<String, Object> variables
    ) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.name = name;
        this.businessKey = businessKey;
        this.variables = variables;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
