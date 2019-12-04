package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;

/**

 */
public class ActivitySignaledEventHandler extends AbstractDatabaseEventLoggerEventHandler {

  @Override
  public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
    ActivitiSignalEvent signalEvent = (ActivitiSignalEvent) event;

    Map<String, Object> data = new HashMap<String, Object>();
    putInMapIfNotNull(data, Fields.ACTIVITY_ID, signalEvent.getActivityId());
    putInMapIfNotNull(data, Fields.ACTIVITY_NAME, signalEvent.getActivityName());
    putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, signalEvent.getProcessDefinitionId());
    putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, signalEvent.getProcessInstanceId());
    putInMapIfNotNull(data, Fields.EXECUTION_ID, signalEvent.getExecutionId());
    putInMapIfNotNull(data, Fields.ACTIVITY_TYPE, signalEvent.getActivityType());
    
    putInMapIfNotNull(data, Fields.SIGNAL_NAME, signalEvent.getSignalName());
    putInMapIfNotNull(data, Fields.SIGNAL_DATA, signalEvent.getSignalData());

    return createEventLogEntry(signalEvent.getProcessDefinitionId(), signalEvent.getProcessInstanceId(), signalEvent.getExecutionId(), null, data);
  }

}
