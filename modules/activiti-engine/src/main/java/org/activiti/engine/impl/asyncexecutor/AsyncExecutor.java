package org.activiti.engine.impl.asyncexecutor;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;

public interface AsyncExecutor {

  public void executeAsyncJob(JobEntity job);
  
  public void setCommandExecutor(CommandExecutor commandExecutor);
  
  public boolean isAutoActivate();

  public void setAutoActivate(boolean isAutoActivate);
  
  public boolean isActive();
  
  public void start();
  
  public void shutdown();
}
