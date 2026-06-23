/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl;

import java.util.Map;
import org.activiti.engine.repository.ProcessDefinition;

public class ProcessInstanceCreationOptions {

    private final ProcessDefinition processDefinition;
    private final String businessKey;
    private final String processInstanceName;
    private final Map<String, Object> variables;
    private final Map<String, Object> transientVariables;
    private final String linkedProcessInstanceId;
    private final String linkedProcessInstanceType;

    private ProcessInstanceCreationOptions(Builder builder) {
        this.processDefinition = builder.processDefinition;
        this.businessKey = builder.businessKey;
        this.processInstanceName = builder.processInstanceName;
        this.variables = builder.variables;
        this.transientVariables = builder.transientVariables;
        this.linkedProcessInstanceId = builder.linkedProcessInstanceId;
        this.linkedProcessInstanceType = builder.linkedProcessInstanceType;
    }

    public static Builder builder(ProcessDefinition processDefinition) {
        return new Builder(processDefinition);
    }

    public static class Builder {

        private final ProcessDefinition processDefinition;
        private String businessKey;
        private String processInstanceName;
        private Map<String, Object> variables;
        private Map<String, Object> transientVariables;
        private String linkedProcessInstanceId;
        private String linkedProcessInstanceType;

        private Builder(ProcessDefinition processDefinition) {
            this.processDefinition = processDefinition;
        }

        public Builder businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public Builder processInstanceName(String processInstanceName) {
            this.processInstanceName = processInstanceName;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder transientVariables(Map<String, Object> transientVariables) {
            this.transientVariables = transientVariables;
            return this;
        }

        public Builder linkedProcessInstanceId(String linkedProcessInstanceId) {
            this.linkedProcessInstanceId = linkedProcessInstanceId;
            return this;
        }

        public Builder linkedProcessInstanceType(String linkedProcessInstanceType) {
            this.linkedProcessInstanceType = linkedProcessInstanceType;
            return this;
        }

        public ProcessInstanceCreationOptions build() {
            return new ProcessInstanceCreationOptions(this);
        }
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Map<String, Object> getTransientVariables() {
        return transientVariables;
    }

    public String getLinkedProcessInstanceId() {
        return linkedProcessInstanceId;
    }

    public String getLinkedProcessInstanceType() {
        return linkedProcessInstanceType;
    }
}
