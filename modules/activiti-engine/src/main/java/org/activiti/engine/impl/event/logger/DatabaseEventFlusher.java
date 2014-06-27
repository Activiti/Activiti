package org.activiti.engine.impl.event.logger;

import org.activiti.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManager;

/**
 * @author Joram Barrez
 */
public class DatabaseEventFlusher extends AbstractEventFlusher {
	
	
	@Override
	public void closing(CommandContext commandContext) {
		EventLogEntryEntityManager eventLogEntryEntityManager = commandContext.getEventLogEntryEntityManager();
		for (EventLoggerEventHandler eventHandler : eventHandlers) {
			eventLogEntryEntityManager.insert(eventHandler.generateEventLogEntry());
		}
	}
	
}
