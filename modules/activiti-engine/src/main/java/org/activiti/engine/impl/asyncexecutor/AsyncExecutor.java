package org.activiti.engine.impl.asyncexecutor;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;

public interface AsyncExecutor {

  public void executeAsyncJob(JobEntity job);
  
  public void setCommandExecutor(CommandExecutor commandExecutor);
  
  public CommandExecutor getCommandExecutor();
  
  public boolean isAutoActivate();

  public void setAutoActivate(boolean isAutoActivate);
  
  public boolean isActive();
  
  public void start();
  
  public void shutdown();
  
  public String getLockOwner();
  
  public int getTimerLockTimeInMillis();
  
  public void setTimerLockTimeInMillis(int lockTimeInMillis);
  
  public int getAsyncJobLockTimeInMillis();
  
  public void setAsyncJobLockTimeInMillis(int lockTimeInMillis);
  
  public int getDefaultTimerJobAcquireWaitTimeInMillis();
  
  public void setDefaultTimerJobAcquireWaitTimeInMillis(int waitTimeInMillis);
  
  public int getDefaultAsyncJobAcquireWaitTimeInMillis();
  
  public void setDefaultAsyncJobAcquireWaitTimeInMillis(int waitTimeInMillis);
  
  public int getMaxAsyncJobsDuePerAcquisition();
  
  public void setMaxAsyncJobsDuePerAcquisition(int maxJobs);
  
  public int getMaxTimerJobsPerAcquisition();
  
  public void setMaxTimerJobsPerAcquisition(int maxJobs);
}
