package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.cmd.DecrementJobRetriesCmd;
import org.activiti.engine.impl.interceptor.Command;


public class DefaultFailedJobCommandFactory implements FailedJobCommandFactory {

  public Command<Object> getCommand(String jobId, Throwable exception) {
    return new DecrementJobRetriesCmd(jobId, exception);
  }

}
