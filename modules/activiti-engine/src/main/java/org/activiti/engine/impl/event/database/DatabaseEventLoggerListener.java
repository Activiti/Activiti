package org.activiti.engine.impl.event.database;

/**
 * @author Joram Barrez
 */
public interface DatabaseEventLoggerListener {
	
	void eventsAdded(DatabaseEventLogger databaseEventLogger);

}
