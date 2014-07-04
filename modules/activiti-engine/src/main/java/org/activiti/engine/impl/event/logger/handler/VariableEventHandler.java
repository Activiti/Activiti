package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;

/**
 * @author Joram Barrez
 */
public abstract class VariableEventHandler extends AbstractDatabaseEventLoggerEventHandler {
	
	protected Map<String, Object> createData(ActivitiVariableEvent variableEvent) {
	  Map<String, Object> data = new HashMap<String, Object>();
		putInMapIfNotNull(data, Fields.NAME, variableEvent.getVariableName());
		putInMapIfNotNull(data, Fields.VALUE, variableEvent.getVariableValue());
		putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, variableEvent.getProcessDefinitionId());
		putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, variableEvent.getProcessInstanceId());
		putInMapIfNotNull(data, Fields.EXECUTION_ID, variableEvent.getExecutionId());
	  return data;
  }


}
