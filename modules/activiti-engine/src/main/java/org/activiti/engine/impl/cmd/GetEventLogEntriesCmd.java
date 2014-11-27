package org.activiti.engine.impl.cmd;

import java.util.List;

import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * @author Joram Barrez
 */
public class GetEventLogEntriesCmd implements Command<List<EventLogEntry>> {
	
  protected String processInstanceId = null;
	protected Long startLogNr = null;
	protected Long pageSize = null;
	
	public GetEventLogEntriesCmd() {
		
	}
	
	public GetEventLogEntriesCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
	}
	
	public GetEventLogEntriesCmd(Long startLogNr, Long pageSize) {
		this.startLogNr = startLogNr;
		this.pageSize = pageSize;
	}
	
	@Override
	public List<EventLogEntry> execute(CommandContext commandContext) {
	  if (processInstanceId != null) {
	    return commandContext.getEventLogEntryEntityManager().findEventLogEntriesByProcessInstanceId(processInstanceId);
	  
	  } else if (startLogNr != null) {
	    return commandContext.getEventLogEntryEntityManager().findEventLogEntries(
          startLogNr,
          pageSize != null ? pageSize : -1);
		
	  } else {
	    return commandContext.getEventLogEntryEntityManager().findAllEventLogEntries();
	  }
	}

}
