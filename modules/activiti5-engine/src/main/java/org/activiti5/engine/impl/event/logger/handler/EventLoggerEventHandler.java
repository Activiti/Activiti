package org.activiti5.engine.impl.event.logger.handler;

import java.util.Date;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.EventLogEntryEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
public interface EventLoggerEventHandler {
	
	EventLogEntryEntity generateEventLogEntry(CommandContext commandContext);
	
	void setEvent(ActivitiEvent event);
	
	void setTimeStamp(Date timeStamp);
	
	void setObjectMapper(ObjectMapper objectMapper);
	
}
