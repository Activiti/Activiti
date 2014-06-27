package org.activiti.engine.impl.event.logger.handler;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
public abstract class AbstractDatabaseEventLoggerEventHandler implements EventLoggerEventHandler {

	protected ActivitiEvent event;
	protected Date timeStamp;
	protected ObjectMapper objectMapper;
	
	public AbstractDatabaseEventLoggerEventHandler() {
	}
	
	protected EventLogEntryEntity createEventLogEntry(Map<String, Object> data) {
		return createEventLogEntry(null, null, null, null, data);
	}
	
	protected EventLogEntryEntity createEventLogEntry(String processDefinitionId, String processInstanceId, 
			String executionId, String taskId, Map<String, Object> data) {
		return createEventLogEntry(event.getType().name(), processDefinitionId, processInstanceId, executionId, taskId, data);
	}
	
	protected EventLogEntryEntity createEventLogEntry(String type, String processDefinitionId, String processInstanceId, 
			String executionId, String taskId, Map<String, Object> data) {
		try {
			
			EventLogEntryEntity eventLogEntry = new EventLogEntryEntity();
			eventLogEntry.setProcessDefinitionId(processDefinitionId);
			eventLogEntry.setProcessInstanceId(processInstanceId);
			eventLogEntry.setExecutionId(executionId);
			eventLogEntry.setTaskId(taskId);
			eventLogEntry.setType(type);
			eventLogEntry.setTimeStamp(timeStamp);
			putInMapIfNotNull(data, Fields.TIMESTAMP, timeStamp);
			
			String userId = Authentication.getAuthenticatedUserId();
			if (userId != null) {
				eventLogEntry.setUserId(userId);
				putInMapIfNotNull(data, "userId", userId);
			}
			
			eventLogEntry.setData(objectMapper.writeValueAsString(data));
		
			return eventLogEntry;
			
		} catch (Exception e) {
			// TODO: what to do here?
		}
		
		return null;
	}

	@Override
	public void setEvent(ActivitiEvent event) {
		this.event = event;
	}
	
	@Override
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	@Override
  public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
  }
	
	// Helper methods //////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
  public <T> T getEntityFromEvent() {
		return (T) ((ActivitiEntityEvent) event).getEntity();
	}
	
	public void putInMapIfNotNull(Map<String, Object> map, String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}
	
}
