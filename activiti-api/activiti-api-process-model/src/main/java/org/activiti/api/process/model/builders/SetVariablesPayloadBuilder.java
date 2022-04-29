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

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.SetProcessVariablesPayload;

public class SetVariablesPayloadBuilder {

    private String processInstanceId;
    private Map<String, Object> variables = new HashMap<>();

    public SetVariablesPayloadBuilder() {
    }

    public SetVariablesPayloadBuilder(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public SetVariablesPayloadBuilder(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
    }

    public SetVariablesPayloadBuilder withProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public SetVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstanceId = processInstance.getId();
        return this;
    }

    public SetVariablesPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SetVariablesPayloadBuilder withVariable(String name,
                                                   Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public SetProcessVariablesPayload build() {
        return new SetProcessVariablesPayload(processInstanceId,
                                              variables);
    }
}
