package org.activiti.engine.impl.event.database;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.event.database.handler.DatabaseEventLoggerEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManager;

/**
 * @author Joram Barrez
 */
public class DatabaseEventFlusher implements CommandContextCloseListener {
	
	protected List<DatabaseEventLoggerEventHandler> eventHandlers = new ArrayList<DatabaseEventLoggerEventHandler>();
	
	@Override
	public void closing(CommandContext commandContext) {
		EventLogEntryEntityManager eventLogEntryEntityManager = commandContext.getEventLogEntryEntityManager();
		for (DatabaseEventLoggerEventHandler eventHandler : eventHandlers) {
			eventLogEntryEntityManager.insert(eventHandler.generateEventLogEntry());
		}
	}
	
	@Override
	public void closed(CommandContext commandContext) {
		// Not interested in closed
	}
	
	public List<DatabaseEventLoggerEventHandler> getEventHandlers() {
		return eventHandlers;
	}

	public void setEventHandlers(List<DatabaseEventLoggerEventHandler> eventHandlers) {
		this.eventHandlers = eventHandlers;
	}
	
	public void addEventHandler(DatabaseEventLoggerEventHandler databaseEventLoggerEventHandler) {
		eventHandlers.add(databaseEventLoggerEventHandler);
	}
	
}
