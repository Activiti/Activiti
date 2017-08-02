/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.history.events;

import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.activiti.services.history.converter.TaskJpaJsonConverter;
import org.activiti.services.core.model.Task;

@Entity
@DiscriminatorValue(value = "TaskCreatedEvent")
public class TaskCreatedEventEntity extends ProcessEngineEventEntity {

    @Convert(converter = TaskJpaJsonConverter.class)
    private Task task;

    public TaskCreatedEventEntity() {
    }

    public TaskCreatedEventEntity(Long id,
                                  Long timestamp,
                                  String eventType,
                                  String executionId,
                                  String processDefinitionId,
                                  String processInstanceId,
                                  Task task) {
        super(id,
              timestamp,
              eventType,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
