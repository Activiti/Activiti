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

import org.activiti.runtime.api.cmd.CreateTask;
import org.activiti.runtime.api.cmd.TaskCommands;

public class CreateTaskImpl extends CommandImpl<TaskCommands> implements CreateTask {

    private String name;
    private String description;

    public CreateTaskImpl() {
    }

    public CreateTaskImpl(String name,
                          String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getTaskId() {
        return null; // not needed for creating a new task
    }

    @Override
    public TaskCommands getCommandType() {
        return TaskCommands.CREATE_TASK;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
