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

package org.activiti.runtime.api.cmd.impl;

import java.util.Map;

import org.activiti.runtime.api.cmd.SetTaskVariables;
import org.activiti.runtime.api.cmd.TaskCommands;

public class SetTaskVariablesImpl extends CommandImpl<TaskCommands> implements SetTaskVariables {

    private String taskId;
    private Map<String, Object> variables;

    public SetTaskVariablesImpl() {
    }

    public SetTaskVariablesImpl(String taskId,
                                Map<String, Object> variables) {
        this.taskId = taskId;
        this.variables = variables;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public TaskCommands getCommandType() {
        return TaskCommands.SET_TASK_VARIABLES;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }
}
