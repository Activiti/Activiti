package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.interceptor.Command;

@Internal
public interface FailedJobCommandFactory {

  public Command<Object> getCommand(String jobId, Throwable exception);

}
