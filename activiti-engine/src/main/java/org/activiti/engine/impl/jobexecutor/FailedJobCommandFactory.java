package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.interceptor.Command;

public interface FailedJobCommandFactory {

  public Command<Object> getCommand(String jobId, Throwable exception);

}
