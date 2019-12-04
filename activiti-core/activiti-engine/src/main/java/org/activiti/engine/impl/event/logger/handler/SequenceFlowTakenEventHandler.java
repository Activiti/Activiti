package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiSequenceFlowTakenEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;

/**

 */
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
