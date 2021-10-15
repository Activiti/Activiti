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
package org.activiti.api.task.model.builders;

import java.util.HashMap;
import java.util.Map;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;

public class CompleteTaskPayloadBuilder {

    private String taskId;
    private Map<String, Object> variables;

    public CompleteTaskPayloadBuilder withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public CompleteTaskPayloadBuilder withVariables(
        Map<String, Object> variables
    ) {
        this.variables = variables;
        return this;
    }

    public CompleteTaskPayloadBuilder withVariable(String name, Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name, value);
        return this;
    }

    public CompleteTaskPayload build() {
        return new CompleteTaskPayload(taskId, variables);
    }
}
