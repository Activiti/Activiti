package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEntityWithVariablesEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**

 */
public class ProcessInstanceStartedEventHandler extends AbstractDatabaseEventLoggerEventHandler {

  private static final String TYPE = "PROCESSINSTANCE_START";

  @Override
  public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {

    ActivitiEntityWithVariablesEvent eventWithVariables = (ActivitiEntityWithVariablesEvent) event;
    ExecutionEntity processInstanceEntity = (ExecutionEntity) eventWithVariables.getEntity();

    Map<String, Object> data = new HashMap<String, Object>();
    putInMapIfNotNull(data, Fields.ID, processInstanceEntity.getId());
    putInMapIfNotNull(data, Fields.BUSINESS_KEY, processInstanceEntity.getBusinessKey());
    putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, processInstanceEntity.getProcessDefinitionId());
    putInMapIfNotNull(data, Fields.NAME, processInstanceEntity.getName());
    putInMapIfNotNull(data, Fields.CREATE_TIME, timeStamp);

    if (eventWithVariables.getVariables() != null && !eventWithVariables.getVariables().isEmpty()) {
      Map<String, Object> variableMap = new HashMap<String, Object>();
      for (Object variableName : eventWithVariables.getVariables().keySet()) {
        putInMapIfNotNull(variableMap, (String) variableName, eventWithVariables.getVariables().get(variableName));
      }
      putInMapIfNotNull(data, Fields.VARIABLES, variableMap);
    }

    return createEventLogEntry(TYPE, processInstanceEntity.getProcessDefinitionId(), processInstanceEntity.getId(), null, null, data);
  }

}
