package org.activiti5.engine.impl.cmd;

import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;

/**
 * @author Joram Barrez
 */
public class DeleteEventLogEntry implements Command<Void> {
	
	protected long logNr;
	
	public DeleteEventLogEntry(long logNr) {
		this.logNr = logNr;
	}
	
	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getEventLogEntryEntityManager().deleteEventLogEntry(logNr);
		return null;
	}

}
