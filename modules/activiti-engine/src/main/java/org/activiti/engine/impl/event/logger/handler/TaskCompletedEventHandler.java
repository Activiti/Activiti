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
public class TaskCompletedEventHandler extends AbstractDatabaseEventLoggerEventHandler {
	
  @Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {

	  ActivitiEntityWithVariablesEvent eventWithVariables = (ActivitiEntityWithVariablesEvent) event;
		TaskEntity task = (TaskEntity) eventWithVariables.getEntity();
		
		long duration = timeStamp.getTime() - task.getCreateTime().getTime();
		
		Map<String, Object> data = new HashMap<String, Object>();
		putInMapIfNotNull(data, Fields.ID, task.getId());
		putInMapIfNotNull(data, Fields.NAME, task.getName());
    putInMapIfNotNull(data, Fields.TASK_DEFINITION_KEY, task.getTaskDefinitionKey());
    putInMapIfNotNull(data, Fields.DESCRIPTION, task.getDescription());
		putInMapIfNotNull(data, Fields.ASSIGNEE, task.getAssignee());
		putInMapIfNotNull(data, Fields.OWNER, task.getOwner());
		putInMapIfNotNull(data, Fields.CATEGORY, task.getCategory());
		putInMapIfNotNull(data, Fields.CREATE_TIME, task.getCreateTime());
		putInMapIfNotNull(data, Fields.DURATION, duration);
		putInMapIfNotNull(data, Fields.DUE_DATE, task.getDueDate());
		putInMapIfNotNull(data, Fields.FORM_KEY, task.getFormKey());
		putInMapIfNotNull(data, Fields.PRIORITY, task.getPriority());
		putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, task.getProcessDefinitionId());
		putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, task.getProcessInstanceId());
		putInMapIfNotNull(data, Fields.EXECUTION_ID, task.getExecutionId());
		putInMapIfNotNull(data, Fields.TENANT_ID, task.getTenantId());
		
		if (eventWithVariables.getVariables() != null && eventWithVariables.getVariables().size() > 0) {
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
