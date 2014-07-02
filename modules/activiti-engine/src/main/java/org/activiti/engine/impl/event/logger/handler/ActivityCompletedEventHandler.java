package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;

/**
 * @author Joram Barrez
 */
public class ActivityCompletedEventHandler extends AbstractDatabaseEventLoggerEventHandler {
	
	@Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
		ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) event;
		
		Map<String, Object> data = new HashMap<String, Object>();
		putInMapIfNotNull(data, Fields.ACTIVITY_ID, activityEvent.getActivityId());
		putInMapIfNotNull(data, Fields.ACTIVITY_NAME, activityEvent.getActivityName());
		putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, activityEvent.getProcessDefinitionId());
		putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, activityEvent.getProcessInstanceId());
		putInMapIfNotNull(data, Fields.EXECUTION_ID, activityEvent.getExecutionId());
		putInMapIfNotNull(data, Fields.ACTIVITY_TYPE, activityEvent.getActivityType());
		putInMapIfNotNull(data, Fields.BEHAVIOR_CLASS, activityEvent.getBehaviorClass());
		
		return createEventLogEntry(activityEvent.getProcessDefinitionId(), activityEvent.getProcessInstanceId(), 
				activityEvent.getExecutionId(), null, data);
	}

}
