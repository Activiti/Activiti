package org.activiti.engine.event;

import java.util.Date;

/**
 * @author Joram Barrez
 */
public interface EventLogEntry {
	
		long getLogNumber();
		
		String getType();
		
		String getProcessDefinitionId();
		
		String getProcessInstanceId();
		
		String getExecutionId();
		
		String getTaskId();
		
		Date getTimeStamp();
		
		String getUserId();
		
		byte[] getData();
		
}
