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

import java.util.Date;

import javax.persistence.EntityManager;

import org.activiti.engine.ActivitiException;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.events.TaskCreatedEvent;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskCreatedEventHandler implements QueryEventHandler {

    private final TaskRepository taskRepository;
    private final EntityManager entityManager;

    @Autowired
    public TaskCreatedEventHandler(TaskRepository taskRepository, EntityManager entityManager) {
        this.taskRepository = taskRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void handle(ProcessEngineEvent event) {
        TaskCreatedEvent taskCreatedEvent = (TaskCreatedEvent) event;
        Task task = taskCreatedEvent.getTask();
        
        // Get processInstance reference proxy without database query
        ProcessInstance processInstance = entityManager
                .getReference(ProcessInstance.class, taskCreatedEvent.getProcessInstanceId());

        // Associate task with parent reference
        task.setProcessInstance(processInstance);
        
        // Set attributes
        task.setStatus("CREATED");
        task.setLastModified(new Date(taskCreatedEvent.getTimestamp()));

        // Persist into database
        try {
            taskRepository.save(task);
        } catch(Exception cause) {
        	throw new ActivitiException("Error handling TaskCreatedEvent["+event+"]", cause);
        }
    }

    @Override
    public Class<? extends ProcessEngineEvent> getHandledEventClass() {
        return TaskCreatedEvent.class;
    }
}
