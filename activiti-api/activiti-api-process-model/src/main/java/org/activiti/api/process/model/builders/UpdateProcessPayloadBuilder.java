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

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;

public class UpdateProcessPayloadBuilder {

    private String processInstanceId;
    private String name;
    private String description;
    private String businessKey;

    public UpdateProcessPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateProcessPayloadBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public UpdateProcessPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public UpdateProcessPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public UpdateProcessPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        this.businessKey = processInstance.getBusinessKey();
        this.name = processInstance.getName();
        return this;
    }


    public UpdateProcessPayload build() {
        return new UpdateProcessPayload(processInstanceId,
                name,
                description,
                businessKey);
    }
}
