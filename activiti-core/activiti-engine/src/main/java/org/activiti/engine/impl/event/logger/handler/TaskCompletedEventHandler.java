/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEntityWithVariablesEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**

 */
public class TaskCompletedEventHandler extends AbstractTaskEventHandler {

    @Override
    public EventLogEntryEntity generateEventLogEntry(
        CommandContext commandContext
    ) {
        ActivitiEntityEvent activitiEntityEvent = (ActivitiEntityEvent) event;

        TaskEntity task = (TaskEntity) activitiEntityEvent.getEntity();
        Map<String, Object> data = handleCommonTaskFields(task);

        long duration = timeStamp.getTime() - task.getCreateTime().getTime();
        putInMapIfNotNull(data, Fields.DURATION, duration);

        if (event instanceof ActivitiEntityWithVariablesEvent) {
            ActivitiEntityWithVariablesEvent activitiEntityWithVariablesEvent = (ActivitiEntityWithVariablesEvent) event;
            if (
                activitiEntityWithVariablesEvent.getVariables() != null &&
                !activitiEntityWithVariablesEvent.getVariables().isEmpty()
            ) {
                Map<String, Object> variableMap = new HashMap<String, Object>();
                for (Object variableName : activitiEntityWithVariablesEvent
                    .getVariables()
                    .keySet()) {
                    putInMapIfNotNull(
                        variableMap,
                        (String) variableName,
                        activitiEntityWithVariablesEvent
                            .getVariables()
                            .get(variableName)
                    );
                }
                if (activitiEntityWithVariablesEvent.isLocalScope()) {
                    putInMapIfNotNull(
                        data,
                        Fields.LOCAL_VARIABLES,
                        variableMap
                    );
                } else {
                    putInMapIfNotNull(data, Fields.VARIABLES, variableMap);
                }
            }
        }

        return createEventLogEntry(
            task.getProcessDefinitionId(),
            task.getProcessInstanceId(),
            task.getExecutionId(),
            task.getId(),
            data
        );
    }
}
