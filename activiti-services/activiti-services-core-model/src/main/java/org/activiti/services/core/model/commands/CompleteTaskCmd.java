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

package org.activiti.services.core.model.commands;

import java.util.Map;
import java.util.UUID;

public class CompleteTaskCmd implements Command {
    private String id;
    private String taskId;
    private Map<String, Object> outputVariables;

    public CompleteTaskCmd() {
        this.id = UUID.randomUUID().toString();
    }

    public CompleteTaskCmd(String taskId,
                           Map<String, Object> outputVariables) {
        this();
        this.taskId = taskId;
        this.outputVariables = outputVariables;
    }

    @Override
    public String getId() {
        return id;
    }

    public Map<String, Object> getOutputVariables() {
        return outputVariables;
    }

    public String getTaskId() {
        return taskId;
    }
}
