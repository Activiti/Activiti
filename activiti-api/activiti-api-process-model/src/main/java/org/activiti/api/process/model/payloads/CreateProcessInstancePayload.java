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
package org.activiti.api.process.model.payloads;

import java.util.UUID;
import org.activiti.api.model.shared.Payload;

public class CreateProcessInstancePayload implements Payload {
    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String name;
    private String businessKey;

    public CreateProcessInstancePayload() {
        this.id = UUID.randomUUID().toString();
    }

    public CreateProcessInstancePayload(String processDefinitionId, String processDefinitionKey,
        String name, String businessKey) {
        this();
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.name = name;
        this.businessKey = businessKey;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinessKey() {
        return businessKey;
    }
}
