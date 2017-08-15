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

import com.querydsl.core.types.dsl.BooleanExpression;
import org.activiti.services.query.model.QVariable;
import org.activiti.services.query.model.Task;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.app.repository.VariableRepository;
import org.activiti.services.query.events.VariableDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskVariableDeletedHandler {

    private final VariableRepository variableRepository;

    private final TaskRepository taskRepository;

    private final EntityFinder entityFinder;

    @Autowired
    public TaskVariableDeletedHandler(VariableRepository variableRepository,
                                      TaskRepository taskRepository,
                                      EntityFinder entityFinder) {
        this.variableRepository = variableRepository;
        this.taskRepository = taskRepository;
        this.entityFinder = entityFinder;
    }

    public void handle(VariableDeletedEvent event) {
        String variableName = event.getVariableName();
        String taskId = event.getTaskId();
        BooleanExpression predicate = QVariable.variable.taskId.eq(taskId)
                .and(
                        QVariable.variable.name.eq(variableName)
                );
        Variable variable = entityFinder.findOne(variableRepository,
                                            predicate,
                                            "Unable to find variable with name '" + variableName + "' for task '" + taskId + "'");
        Task task = entityFinder.findById(taskRepository,
                                                     taskId,
                                                     "Unable to find task: " + taskId);

        task.removeVariable(variable);
        taskRepository.save(task);

        variableRepository.delete(variable);
    }
}
