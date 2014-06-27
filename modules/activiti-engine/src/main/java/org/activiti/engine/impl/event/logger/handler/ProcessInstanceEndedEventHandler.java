package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Joram Barrez
 */
public class ProcessInstanceEndedEventHandler extends AbstractDatabaseEventLoggerEventHandler {
	
	private static final String TYPE = "PROCESSINSTANCE-END";

	@Override
  public EventLogEntryEntity generateEventLogEntry() {
		ExecutionEntity processInstanceEntity = getEntityFromEvent(); 
		
		Map<String, Object> data = new HashMap<String, Object>();
		putInMapIfNotNull(data, Fields.ID, processInstanceEntity.getId());
		putInMapIfNotNull(data, Fields.BUSINESS_KEY, processInstanceEntity.getBusinessKey());
		putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, processInstanceEntity.getProcessDefinitionId());
		putInMapIfNotNull(data, Fields.NAME, processInstanceEntity.getName());
		putInMapIfNotNull(data, Fields.TENANT_ID, processInstanceEntity.getTenantId());
		
		return createEventLogEntry(TYPE, processInstanceEntity.getProcessDefinitionId(), processInstanceEntity.getId(), null, null, data);
  }

}
