package org.activiti5.engine.impl.asyncexecutor;

import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.persistence.entity.JobEntity;

public interface AsyncExecutor {

  void executeAsyncJob(JobEntity job);
  
  void setCommandExecutor(CommandExecutor commandExecutor);
  
  CommandExecutor getCommandExecutor();
  
  boolean isAutoActivate();

  void setAutoActivate(boolean isAutoActivate);
  
  boolean isActive();
  
  void start();
  
  void shutdown();
  
  String getLockOwner();
  
  int getTimerLockTimeInMillis();
  
  void setTimerLockTimeInMillis(int lockTimeInMillis);
  
  int getAsyncJobLockTimeInMillis();
  
  void setAsyncJobLockTimeInMillis(int lockTimeInMillis);
  
  int getDefaultTimerJobAcquireWaitTimeInMillis();
  
  void setDefaultTimerJobAcquireWaitTimeInMillis(int waitTimeInMillis);
  
  int getDefaultAsyncJobAcquireWaitTimeInMillis();
  
  void setDefaultAsyncJobAcquireWaitTimeInMillis(int waitTimeInMillis);
  
  int getMaxAsyncJobsDuePerAcquisition();
  
  void setMaxAsyncJobsDuePerAcquisition(int maxJobs);
  
  int getMaxTimerJobsPerAcquisition();
  
  void setMaxTimerJobsPerAcquisition(int maxJobs);
  
  int getRetryWaitTimeInMillis();
  
  void setRetryWaitTimeInMillis(int retryWaitTimeInMillis);
  
}
