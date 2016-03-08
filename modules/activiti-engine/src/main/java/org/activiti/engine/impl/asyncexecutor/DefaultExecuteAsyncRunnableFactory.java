package org.activiti.engine.impl.asyncexecutor;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;

public class DefaultExecuteAsyncRunnableFactory implements ExecuteAsyncRunnableFactory {

  @Override
  public Runnable createExecuteAsyncRunnable(JobEntity jobEntity, CommandExecutor commandExecutor) {
    return new ExecuteAsyncRunnable(jobEntity, commandExecutor);
  }
}
