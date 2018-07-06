/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.runtime.api.model.builder.CompleteTaskPayload;

public class CompleteTaskPayloadImpl implements CompleteTaskPayload {

    private final TaskService taskService;
    private final String taskId;

    private Map<String, Object> variables = new HashMap<>();

    public CompleteTaskPayloadImpl(TaskService taskService,
                                   String taskId) {
        this.taskService = taskService;
        this.taskId = taskId;
    }

    @Override
    public CompleteTaskPayload variable(String name,
                                        Object value) {
        variables.put(name,
                      value);
        return this;
    }

    @Override
    public CompleteTaskPayload variables(Map<String, Object> variables) {
        if (variables != null) {
            this.variables.putAll(variables);
        }
        return this;
    }

    @Override
    public Void doIt() {
        taskService.complete(taskId,
                             variables);
        return null;
    }
}
