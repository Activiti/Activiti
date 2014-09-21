package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEntityWithVariablesEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**
 * @author Joram Barrez
 */
public class TaskCompletedEventHandler extends AbstractTaskEventHandler {
	
  @Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {

	  ActivitiEntityWithVariablesEvent eventWithVariables = (ActivitiEntityWithVariablesEvent) event;
		TaskEntity task = (TaskEntity) eventWithVariables.getEntity();
		Map<String, Object> data = handleCommonTaskFields(task);
		
		long duration = timeStamp.getTime() - task.getCreateTime().getTime();
		putInMapIfNotNull(data, Fields.DURATION, duration);
		
		if (eventWithVariables.getVariables() != null && !eventWithVariables.getVariables().isEmpty()) {
		  Map<String, Object> variableMap = new HashMap<String, Object>();
		  for (Object variableName : eventWithVariables.getVariables().keySet()) {
        putInMapIfNotNull(variableMap, (String) variableName, eventWithVariables.getVariables().get(variableName));
      }
		  if (eventWithVariables.isLocalScope()) {
		    putInMapIfNotNull(data, Fields.LOCAL_VARIABLES, variableMap);
		  } else {
		    putInMapIfNotNull(data, Fields.VARIABLES, variableMap);
		  }
		}
		
    return createEventLogEntry(task.getProcessDefinitionId(), task.getProcessInstanceId(), task.getExecutionId(), task.getId(), data);
	}

}
