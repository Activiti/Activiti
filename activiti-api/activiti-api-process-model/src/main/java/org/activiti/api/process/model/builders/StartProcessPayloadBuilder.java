/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.process.model.payloads.StartProcessPayload;

public class StartProcessPayloadBuilder {

    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;
    private String businessKey;
    private Map<String, Object> variables = new HashMap<>();

    public StartProcessPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public StartProcessPayloadBuilder withVariable(String name,
                                                   Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                value);
        return this;
    }

    public StartProcessPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public StartProcessPayloadBuilder withProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public StartProcessPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public StartProcessPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
        return this;
    }

    public StartProcessPayload build() {
        return new StartProcessPayload(processDefinitionId,
                processDefinitionKey,
                name,
                businessKey,
                variables);
    }
}
