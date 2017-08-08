/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.core.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskVariables {

    public enum TaskVariableScope {
        LOCAL, GLOBAL
    }

    private String taskId;

    private Map<String, Object> variables;

    private TaskVariableScope scope;

    public TaskVariables(String taskId,
                         Map<String, Object> variables,
                         TaskVariableScope scope) {
        this.taskId = taskId;
        this.variables = variables;
        this.scope = scope;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskVariableScope getScope() {
        return scope;
    }
}
