package org.activiti5.engine.impl.event.logger.handler;

import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.EventLogEntryEntity;

/**
 * @author Joram Barrez
 */
public class VariableUpdatedEventHandler extends VariableEventHandler {
	
	@Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
		ActivitiVariableEvent variableEvent = (ActivitiVariableEvent) event;
		Map<String, Object> data = createData(variableEvent);

    return createEventLogEntry(variableEvent.getProcessDefinitionId(), variableEvent.getProcessInstanceId(), 
    		variableEvent.getExecutionId(), variableEvent.getTaskId(), data);
	}

}
