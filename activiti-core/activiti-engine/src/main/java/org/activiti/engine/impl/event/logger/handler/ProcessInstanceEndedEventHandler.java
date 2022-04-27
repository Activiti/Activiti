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

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class ProcessInstanceEndedEventHandler extends AbstractDatabaseEventLoggerEventHandler {

  private static final String TYPE = "PROCESSINSTANCE_END";

  @Override
  public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
    ExecutionEntity processInstanceEntity = getEntityFromEvent();

    Map<String, Object> data = new HashMap<String, Object>();
    putInMapIfNotNull(data, Fields.ID, processInstanceEntity.getId());
    putInMapIfNotNull(data, Fields.BUSINESS_KEY, processInstanceEntity.getBusinessKey());
    putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, processInstanceEntity.getProcessDefinitionId());
    putInMapIfNotNull(data, Fields.NAME, processInstanceEntity.getName());
    putInMapIfNotNull(data, Fields.END_TIME, timeStamp);

    return createEventLogEntry(TYPE, processInstanceEntity.getProcessDefinitionId(), processInstanceEntity.getId(), null, null, data);
  }

}
