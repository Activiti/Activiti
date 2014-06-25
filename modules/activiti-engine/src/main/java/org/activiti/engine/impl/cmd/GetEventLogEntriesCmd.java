package org.activiti.engine.impl.cmd;

import java.util.List;

import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * @author Joram Barrez
 */
public class GetEventLogEntriesCmd implements Command<List<EventLogEntry>> {
	
	protected Long startLogNr = null;
	protected Long pageSize = null;
	
	public GetEventLogEntriesCmd() {
		
	}
	
	public GetEventLogEntriesCmd(Long startLogNr, Long pageSize) {
		this.startLogNr = startLogNr;
		this.pageSize = pageSize;
	}
	
	@Override
	public List<EventLogEntry> execute(CommandContext commandContext) {
		if (startLogNr == null) {
			return commandContext.getEventLogEntryEntityManager().findAllEventLogEntries();
		} else {
			return commandContext.getEventLogEntryEntityManager().findEventLogEntries(
					startLogNr != null ? startLogNr : 0,
					pageSize != null ? pageSize : -1);
		}
	}

}
