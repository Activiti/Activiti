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

package org.activiti.services.events.converter;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.task.Task;
import org.activiti.services.core.model.converter.TaskConverter;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.events.TaskCompletedEventImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.activiti.engine.delegate.event.ActivitiEventType.TASK_COMPLETED;

@Component
public class TaskCompletedEventConverter extends AbstractEventConverter {

    private final TaskConverter taskConverter;

    @Autowired
    public TaskCompletedEventConverter(TaskConverter taskConverter) {
        this.taskConverter = taskConverter;
    }

    @Override
    public ProcessEngineEvent from(ActivitiEvent event) {
        return new TaskCompletedEventImpl(getApplicationName(),
                                          event.getExecutionId(),
                                          event.getProcessDefinitionId(),
                                          event.getProcessInstanceId(),
                                          taskConverter.from((Task) ((ActivitiEntityEventImpl) event).getEntity()));
    }

    @Override
    public ActivitiEventType handledType() {
        return TASK_COMPLETED;
    }
}
