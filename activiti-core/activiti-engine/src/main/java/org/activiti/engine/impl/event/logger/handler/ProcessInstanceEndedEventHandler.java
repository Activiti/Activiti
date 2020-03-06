package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**

 */
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
