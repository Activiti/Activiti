package org.activiti.engine.impl.event.logger;

import org.activiti.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class DatabaseEventFlusher extends AbstractEventFlusher {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseEventFlusher.class);

  @Override
  public void closing(CommandContext commandContext) {
    
    if (commandContext.getException() != null) {
      return; // Not interested in events about exceptions
    }
    
    EventLogEntryEntityManager eventLogEntryEntityManager = commandContext.getEventLogEntryEntityManager();
    for (EventLoggerEventHandler eventHandler : eventHandlers) {
      try {
        eventLogEntryEntityManager.insert(eventHandler.generateEventLogEntry(commandContext), false);
      } catch (Exception e) {
        logger.warn("Could not create event log", e);
      }
    }
  }

  public void afterSessionsFlush(CommandContext commandContext) {
    
  }

  public void closeFailure(CommandContext commandContext) {
    
  }

}
