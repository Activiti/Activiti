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

package org.activiti.services.query.events.handlers;

import org.activiti.services.query.model.Task;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.app.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskVariableCreatedHandler {

    private final TaskRepository taskRepository;

    private final EntityFinder entityFinder;

    private final VariableRepository variableRepository;

    @Autowired
    public TaskVariableCreatedHandler(TaskRepository taskRepository,
                                      EntityFinder entityFinder,
                                      VariableRepository variableRepository) {
        this.taskRepository = taskRepository;
        this.entityFinder = entityFinder;
        this.variableRepository = variableRepository;
    }

    public void handle(Variable variable) {
        String taskId = variable.getTaskId();
        Task task = entityFinder.findById(taskRepository,
                                          taskId,
                                          "Unable to find task for the given id: " + taskId);
        variableRepository.save(variable);

        task.addVariable(variable);
        taskRepository.save(task);
    }

}
