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

import java.util.HashSet;
import java.util.Set;

import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;

public class GetProcessInstancesPayloadBuilder {

    private String businessKey;
    private Set<String> processDefinitionKeys = new HashSet<>();
    private boolean suspendedOnly = false;
    private boolean activeOnly = false;
    private String parentProcessInstanceId;


    public GetProcessInstancesPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;
        return this;
    }

    public GetProcessInstancesPayloadBuilder withProcessDefinitionKeys(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
        return this;
    }

    public GetProcessInstancesPayloadBuilder withProcessDefinitionKey(String processDefinitionKey) {
        if (processDefinitionKeys == null) {
            processDefinitionKeys = new HashSet<>();
        }
        processDefinitionKeys.add(processDefinitionKey);
        return this;
    }

    public GetProcessInstancesPayloadBuilder suspended() {
        this.suspendedOnly = true;
        return this;
    }

    public GetProcessInstancesPayloadBuilder active() {
        this.activeOnly = true;
        return this;
    }

    public GetProcessInstancesPayloadBuilder withParentProcessInstanceId(String parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
        return this;
    }


    public GetProcessInstancesPayload build() {
        GetProcessInstancesPayload getProcessInstancesPayload = new GetProcessInstancesPayload();
        getProcessInstancesPayload.setBusinessKey(businessKey);
        getProcessInstancesPayload.setProcessDefinitionKeys(processDefinitionKeys);
        getProcessInstancesPayload.setActiveOnly(activeOnly);
        getProcessInstancesPayload.setSuspendedOnly(suspendedOnly);
        getProcessInstancesPayload.setParentProcessInstanceId(parentProcessInstanceId);
        return getProcessInstancesPayload;
    }
}
