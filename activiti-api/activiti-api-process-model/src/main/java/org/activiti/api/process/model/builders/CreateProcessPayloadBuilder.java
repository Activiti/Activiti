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

import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;

public class CreateProcessPayloadBuilder {

    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;
    private String businessKey;

    public CreateProcessPayloadBuilder() {
    }

    public CreateProcessPayloadBuilder withProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public CreateProcessPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateProcessPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
        return this;
    }

    public CreateProcessPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public CreateProcessInstancePayload build() {
        return new CreateProcessInstancePayload(processDefinitionId,
            processDefinitionKey,
            name,
            businessKey);
    }
}
