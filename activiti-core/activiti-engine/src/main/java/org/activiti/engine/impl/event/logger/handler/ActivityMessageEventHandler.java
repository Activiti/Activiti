/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;


public class ActivityMessageEventHandler extends AbstractDatabaseEventLoggerEventHandler {

  @Override
  public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) event;

    Map<String, Object> data = new HashMap<String, Object>();
    putInMapIfNotNull(data, Fields.ACTIVITY_ID, messageEvent.getActivityId());
    putInMapIfNotNull(data, Fields.ACTIVITY_NAME, messageEvent.getActivityName());
    putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, messageEvent.getProcessDefinitionId());
    putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, messageEvent.getProcessInstanceId());
    putInMapIfNotNull(data, Fields.EXECUTION_ID, messageEvent.getExecutionId());
    putInMapIfNotNull(data, Fields.ACTIVITY_TYPE, messageEvent.getActivityType());

    putInMapIfNotNull(data, Fields.MESSAGE_NAME, messageEvent.getMessageName());
    putInMapIfNotNull(data, Fields.MESSAGE_DATA, messageEvent.getMessageData());

    return createEventLogEntry(messageEvent.getProcessDefinitionId(), messageEvent.getProcessInstanceId(), messageEvent.getExecutionId(), null, data);
  }

}
