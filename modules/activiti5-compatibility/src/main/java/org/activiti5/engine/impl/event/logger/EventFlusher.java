package org.activiti5.engine.impl.event.logger;

import java.util.List;

import org.activiti5.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti5.engine.impl.interceptor.CommandContextCloseListener;

/**
 * @author Joram Barrez
 */
public interface EventFlusher extends CommandContextCloseListener {
	
	List<EventLoggerEventHandler> getEventHandlers();

	void setEventHandlers(List<EventLoggerEventHandler> eventHandlers);
	
	void addEventHandler(EventLoggerEventHandler databaseEventLoggerEventHandler);
	
}
