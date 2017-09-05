package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**

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
