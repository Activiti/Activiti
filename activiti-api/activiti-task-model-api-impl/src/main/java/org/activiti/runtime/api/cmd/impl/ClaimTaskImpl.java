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

import org.activiti.runtime.api.cmd.ClaimTask;
import org.activiti.runtime.api.cmd.TaskCommands;

public class ClaimTaskImpl extends CommandImpl<TaskCommands> implements ClaimTask {

    private String taskId;
    private String assignee;
    private TaskCommands commandType = TaskCommands.CLAIM_TASK;

    public ClaimTaskImpl() {
    }

    public ClaimTaskImpl(String taskId,
                         String assignee) {
        this.taskId = taskId;
        this.assignee = assignee;
    }

    @Override
    public TaskCommands getCommandType() {
        return commandType;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getAssignee() {
        return assignee;
    }
}
