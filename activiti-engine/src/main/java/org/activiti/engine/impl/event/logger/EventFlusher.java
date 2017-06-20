package org.activiti.engine.impl.event.logger;

import java.util.List;

import org.activiti.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;

/**

 */
public interface EventFlusher extends CommandContextCloseListener {

  List<EventLoggerEventHandler> getEventHandlers();

  void setEventHandlers(List<EventLoggerEventHandler> eventHandlers);

  void addEventHandler(EventLoggerEventHandler databaseEventLoggerEventHandler);

}
