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

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.RemoveProcessVariablesPayload;

public class RemoveVariablesPayloadBuilder {

    private String processInstanceId;
    private List<String> variableNames = new ArrayList<>();

    public RemoveVariablesPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public RemoveVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public RemoveVariablesPayloadBuilder withVariableNames(String variableName) {
        if (variableNames == null) {
            variableNames = new ArrayList<>();
        }
        variableNames.add(variableName);
        return this;
    }

    public RemoveVariablesPayloadBuilder withVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
        return this;
    }

    public RemoveProcessVariablesPayload build() {
        return new RemoveProcessVariablesPayload(processInstanceId,
                                                 variableNames);
    }
}
