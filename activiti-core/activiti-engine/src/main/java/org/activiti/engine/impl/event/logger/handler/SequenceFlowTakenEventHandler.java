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

import org.activiti.engine.delegate.event.ActivitiSequenceFlowTakenEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;


public class SequenceFlowTakenEventHandler extends AbstractDatabaseEventLoggerEventHandler {

  @Override
  public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
    ActivitiSequenceFlowTakenEvent sequenceFlowTakenEvent = (ActivitiSequenceFlowTakenEvent) event;

    Map<String, Object> data = new HashMap<String, Object>();
    putInMapIfNotNull(data, Fields.ID, sequenceFlowTakenEvent.getId());

    putInMapIfNotNull(data, Fields.SOURCE_ACTIVITY_ID, sequenceFlowTakenEvent.getSourceActivityId());
    putInMapIfNotNull(data, Fields.SOURCE_ACTIVITY_NAME, sequenceFlowTakenEvent.getSourceActivityName());
    putInMapIfNotNull(data, Fields.SOURCE_ACTIVITY_TYPE, sequenceFlowTakenEvent.getSourceActivityType());
    putInMapIfNotNull(data, Fields.SOURCE_ACTIVITY_BEHAVIOR_CLASS, sequenceFlowTakenEvent.getSourceActivityBehaviorClass());

    putInMapIfNotNull(data, Fields.TARGET_ACTIVITY_ID, sequenceFlowTakenEvent.getTargetActivityId());
    putInMapIfNotNull(data, Fields.TARGET_ACTIVITY_NAME, sequenceFlowTakenEvent.getTargetActivityName());
    putInMapIfNotNull(data, Fields.TARGET_ACTIVITY_TYPE, sequenceFlowTakenEvent.getTargetActivityType());
    putInMapIfNotNull(data, Fields.TARGET_ACTIVITY_BEHAVIOR_CLASS, sequenceFlowTakenEvent.getTargetActivityBehaviorClass());

    return createEventLogEntry(event.getProcessDefinitionId(), event.getProcessInstanceId(), event.getExecutionId(), null, data);
  }

}
