package org.activiti5.engine.impl.event.logger;

import org.activiti5.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.EventLogEntryEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class DatabaseEventFlusher extends AbstractEventFlusher {
	
	private static final Logger logger = LoggerFactory.getLogger(DatabaseEventFlusher.class);
	
	@Override
	public void closing(CommandContext commandContext) {
		EventLogEntryEntityManager eventLogEntryEntityManager = commandContext.getEventLogEntryEntityManager();
		for (EventLoggerEventHandler eventHandler : eventHandlers) {
			try {
				eventLogEntryEntityManager.insert(eventHandler.generateEventLogEntry(commandContext));
			} catch (Exception e) {
				logger.warn("Could not create event log", e);
			}
		}
	}
	
}
