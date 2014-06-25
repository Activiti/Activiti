package org.activiti.engine.impl.event.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.event.database.handler.ActivityCompletedEventHandler;
import org.activiti.engine.impl.event.database.handler.ActivityStartedEventHandler;
import org.activiti.engine.impl.event.database.handler.DatabaseEventLoggerEventHandler;
import org.activiti.engine.impl.event.database.handler.ProcessInstanceEndedEventHandler;
import org.activiti.engine.impl.event.database.handler.ProcessInstanceStartedEventHandler;
import org.activiti.engine.impl.event.database.handler.SequenceFlowTakenEventHandler;
import org.activiti.engine.impl.event.database.handler.TaskCompletedEventHandler;
import org.activiti.engine.impl.event.database.handler.TaskCreatedEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
public class DatabaseEventLogger implements ActivitiEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DatabaseEventLogger.class);
	
	protected Clock clock;
	protected ObjectMapper objectMapper;
	
	// Transaction listeners
	protected DatabaseEventFlusher databaseEventFlusher = null;
	
	// Mapping of type -> handler
	protected Map<ActivitiEventType, Class<? extends DatabaseEventLoggerEventHandler>> eventHandlers 
		= new HashMap<ActivitiEventType, Class<? extends DatabaseEventLoggerEventHandler>>();
	
	// Listeners for new events
	protected List<DatabaseEventLoggerListener> listeners;
	
	public DatabaseEventLogger(Clock clock) {
		this.clock = clock;
		this.objectMapper = new ObjectMapper();
		
		// Initialization of all event handlers
		
		// Engine lifecycle events
		
//		addEventHandler(ActivitiEventType.ENGINE_CREATED, EngineCreatedEventHandler.class);
//		addEventHandler(ActivitiEventType.ENGINE_CLOSED, EngineClosedEventHandler.class);
		
		// Process execution events
		
		addEventHandler(ActivitiEventType.TASK_CREATED, TaskCreatedEventHandler.class);
		addEventHandler(ActivitiEventType.TASK_COMPLETED, TaskCompletedEventHandler.class);
		
		addEventHandler(ActivitiEventType.SEQUENCEFLOW_TAKEN, SequenceFlowTakenEventHandler.class);
		
		addEventHandler(ActivitiEventType.ACTIVITY_COMPLETED, ActivityCompletedEventHandler.class);
		addEventHandler(ActivitiEventType.ACTIVITY_STARTED, ActivityStartedEventHandler.class);
	}
	
	@Override
	public void onEvent(ActivitiEvent event) {
		DatabaseEventLoggerEventHandler eventHandler = getEventHandler(event);
		if (eventHandler != null) {

			// Events are flushed when command context is closed
			if (eventHandler != null && databaseEventFlusher == null) {
				CommandContext currentCommandContext = Context.getCommandContext();
				databaseEventFlusher = new DatabaseEventFlusher();
				currentCommandContext.addCloseListener(databaseEventFlusher);
				currentCommandContext
				    .addCloseListener(new CommandContextCloseListener() {

					    @Override
					    public void closing(CommandContext commandContext) {
					    }

					    @Override
					    public void closed(CommandContext commandContext) {
						    databaseEventFlusher = null;
						    
						    // For those who are interested: we can now broacast the events were added
								if (listeners != null) {
									for (DatabaseEventLoggerListener listener : listeners) {
										listener.eventsAdded(DatabaseEventLogger.this);
									}
								}
					    }
					    
				    });
			}

			databaseEventFlusher.addEventHandler(eventHandler);
		}
	}
	
	// Subclasses can override this if defaults are not ok
	protected DatabaseEventLoggerEventHandler getEventHandler(ActivitiEvent event) {

		Class<? extends DatabaseEventLoggerEventHandler> eventHandlerClass = null;
		if (event.getType().equals(ActivitiEventType.ENTITY_CREATED)) {
			Object entity = ((ActivitiEntityEvent) event).getEntity();
			if (entity instanceof ExecutionEntity) {
				ExecutionEntity executionEntity = (ExecutionEntity) entity;
				if (executionEntity.getProcessInstanceId().equals(executionEntity.getId())) {
					eventHandlerClass = ProcessInstanceStartedEventHandler.class;
				}
			}
		} else if (event.getType().equals(ActivitiEventType.ENTITY_DELETED)) {
			Object entity = ((ActivitiEntityEvent) event).getEntity();
			if (entity instanceof ExecutionEntity) {
				ExecutionEntity executionEntity = (ExecutionEntity) entity;
				if (executionEntity.getProcessInstanceId().equals(executionEntity.getId())) {
					eventHandlerClass = ProcessInstanceEndedEventHandler.class;
				}
			}
		} else {
			// Default: dedicated mapper for the type
			eventHandlerClass = eventHandlers.get(event.getType());
		}
		
		if (eventHandlerClass != null) {
			return instantiateEventHandler(event, eventHandlerClass);
		}
		
		return null;
	}

	protected DatabaseEventLoggerEventHandler instantiateEventHandler(ActivitiEvent event,
      Class<? extends DatabaseEventLoggerEventHandler> eventHandlerClass) {
		try {
			DatabaseEventLoggerEventHandler eventHandler = eventHandlerClass.newInstance();
			eventHandler.setTimeStamp(clock.getCurrentTime());
			eventHandler.setEvent(event);
			eventHandler.setObjectMapper(objectMapper);
			return eventHandler;
		} catch (Exception e) {
			logger.warn("Could not instantiate " + eventHandlerClass + ", this is most likely a programmatic error");
		}
		return null;
  }
	
	@Override
  public boolean isFailOnException() {
		return false;
  }
	
	public void addEventHandler(ActivitiEventType eventType, Class<? extends DatabaseEventLoggerEventHandler> eventHandlerClass) {
		eventHandlers.put(eventType, eventHandlerClass);
	}
	
	public void addDatabaseEventLoggerListener(DatabaseEventLoggerListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<DatabaseEventLoggerListener>(1);
		}
		listeners.add(listener);
	}
	
}
