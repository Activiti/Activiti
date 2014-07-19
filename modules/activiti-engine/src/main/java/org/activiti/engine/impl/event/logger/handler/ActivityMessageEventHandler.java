package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;

/**
 * @author Joram Barrez
 */
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
		putInMapIfNotNull(data, Fields.BEHAVIOR_CLASS, messageEvent.getBehaviorClass());
		
		putInMapIfNotNull(data, Fields.MESSAGE_NAME, messageEvent.getMessageName());
		putInMapIfNotNull(data, Fields.MESSAGE_DATA, messageEvent.getMessageData());
		
		return createEventLogEntry(messageEvent.getProcessDefinitionId(), messageEvent.getProcessInstanceId(), 
				messageEvent.getExecutionId(), null, data);
	}

}
