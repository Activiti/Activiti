/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.asyncexecutor;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.Job;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class DefaultAsyncJobExecutor implements AsyncExecutor {

  private static Logger log = LoggerFactory.getLogger(DefaultAsyncJobExecutor.class);

  /**
   * The minimal number of threads that are kept alive in the threadpool for job execution
   */
  protected int corePoolSize = 2;

  /**
   * The maximum number of threads that are kept alive in the threadpool for job execution
   */
  protected int maxPoolSize = 10;

  /**
   * The time (in milliseconds) a thread used for job execution must be kept alive before it is destroyed. Default setting is 0. Having a non-default setting of 0 takes resources, but in the case of
   * many job executions it avoids creating new threads all the time.
   */
  protected long keepAliveTime = 5000L;

  /** The size of the queue on which jobs to be executed are placed */
  protected int queueSize = 100;

  /** The queue used for job execution work */
  protected BlockingQueue<Runnable> threadPoolQueue;

  /** The executor service used for job execution */
  protected ExecutorService executorService;

  /**
   * The time (in seconds) that is waited to gracefully shut down the threadpool used for job execution
   */
  protected long secondsToWaitOnShutdown = 60L;

  protected Thread timerJobAcquisitionThread;
  protected Thread asyncJobAcquisitionThread;
  protected Thread resetExpiredJobThread;
  
  protected AcquireTimerJobsRunnable timerJobRunnable;
  protected AcquireAsyncJobsDueRunnable asyncJobsDueRunnable;
  protected ResetExpiredJobsRunnable resetExpiredJobsRunnable;
  
  protected ExecuteAsyncRunnableFactory executeAsyncRunnableFactory;

  protected boolean isAutoActivate;
  protected boolean isActive;
  protected boolean isMessageQueueMode;

  protected int maxTimerJobsPerAcquisition = 1;
  protected int maxAsyncJobsDuePerAcquisition = 1;
  protected int defaultTimerJobAcquireWaitTimeInMillis = 10 * 1000;
  protected int defaultAsyncJobAcquireWaitTimeInMillis = 10 * 1000;
  protected int defaultQueueSizeFullWaitTime = 0; 

  protected String lockOwner = UUID.randomUUID().toString();
  protected int timerLockTimeInMillis = 5 * 60 * 1000;
  protected int asyncJobLockTimeInMillis = 5 * 60 * 1000;
  protected int retryWaitTimeInMillis = 500;
  
  protected int resetExpiredJobsInterval = 60 * 1000;
  protected int resetExpiredJobsPageSize = 3;
  
  // Job queue used when async executor is not yet started and jobs are already added.
  // This is mainly used for testing purpose.
  protected LinkedList<Job> temporaryJobQueue = new LinkedList<Job>();
  
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public boolean executeAsyncJob(final Job job) {
    
    if (isMessageQueueMode) {
      // When running with a message queue based job executor,
      // the job is not executed here.
      return true;
    }
    
    Runnable runnable = null;
    if (isActive) {
      runnable = createRunnableForJob(job);
      
      try {
        executorService.execute(runnable);
      } catch (RejectedExecutionException e) {
        
        // When a RejectedExecutionException is caught, this means that the queue for holding the jobs 
        // that are to be executed is full and can't store more.
        // The job is now 'unlocked', meaning that the lock owner/time is set to null,
        // so other executors can pick the job up (or this async executor, the next time the 
        // acquire query is executed.
        
        // This can happen while already in a command context (for example in a transaction listener
        // after the async executor has been hinted that a new async job is created)
        // or not (when executed in the acquire thread runnable)
        
        CommandContext commandContext = Context.getCommandContext();
        if (commandContext != null) {
          commandContext.getJobManager().unacquire(job);
          
        } else {
          processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
            public Void execute(CommandContext commandContext) {
              commandContext.getJobManager().unacquire(job);
              return null;
            }
          });
        }
        
        // Job queue full, returning true so (if wanted) the acquiring can be throttled
        return false;
      }
      
    } else {
      temporaryJobQueue.add(job);
    }
    
    return true;
  }

  protected Runnable createRunnableForJob(final Job job) {
    if (executeAsyncRunnableFactory == null) {
      return new ExecuteAsyncRunnable(job, processEngineConfiguration);
    } else {
      return executeAsyncRunnableFactory.createExecuteAsyncRunnable(job, processEngineConfiguration);
    }
  }
  
  /** Starts the async executor */
  public void start() {
    if (isActive) {
      return;
    }

    log.info("Starting up the default async job executor [{}].", getClass().getName());
    
    if (timerJobRunnable == null) {
      timerJobRunnable = new AcquireTimerJobsRunnable(this, processEngineConfiguration.getJobManager());
    }
    
    if (resetExpiredJobsRunnable == null) {
      resetExpiredJobsRunnable = new ResetExpiredJobsRunnable(this);
    }
    
    if (!isMessageQueueMode && asyncJobsDueRunnable == null) {
      asyncJobsDueRunnable = new AcquireAsyncJobsDueRunnable(this);
    }
    
    if (!isMessageQueueMode) {
      initAsyncJobExecutionThreadPool();
      startJobAcquisitionThread();
    }
    
    startTimerAcquisitionThread();
    startResetExpiredJobsThread();

    isActive = true;

    executeTemporaryJobs();
  }

  protected void executeTemporaryJobs() {
    while (!temporaryJobQueue.isEmpty()) {
      Job job = temporaryJobQueue.pop();
      executeAsyncJob(job);
    }
  }

  /** Shuts down the whole job executor */
  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the default async job executor [{}].", getClass().getName());
    
    if (timerJobRunnable != null) {
      timerJobRunnable.stop();
    }
    if (asyncJobsDueRunnable != null) {
      asyncJobsDueRunnable.stop();
    }
    if (resetExpiredJobsRunnable != null) {
      resetExpiredJobsRunnable.stop();
    }
    
    stopResetExpiredJobsThread();
    stopTimerAcquisitionThread();
    stopJobAcquisitionThread();
    stopExecutingAsyncJobs();

    timerJobRunnable = null;
    asyncJobsDueRunnable = null;
    resetExpiredJobsRunnable = null;
    
    isActive = false;
  }

  protected void initAsyncJobExecutionThreadPool() {
    if (threadPoolQueue == null) {
      log.info("Creating thread pool queue of size {}", queueSize);
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }

    if (executorService == null) {
      log.info("Creating executor service with corePoolSize {}, maxPoolSize {} and keepAliveTime {}", corePoolSize, maxPoolSize, keepAliveTime);

      BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("activiti-async-job-executor-thread-%d").build();
      executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue, threadFactory);
    }
  }

  protected void stopExecutingAsyncJobs() {
    if (executorService != null) {
      
      // Ask the thread pool to finish and exit
      executorService.shutdown();
  
      // Waits for 1 minute to finish all currently executing jobs
      try {
        if (!executorService.awaitTermination(secondsToWaitOnShutdown, TimeUnit.SECONDS)) {
          log.warn("Timeout during shutdown of async job executor. " + "The current running jobs could not end within " + secondsToWaitOnShutdown + " seconds after shutdown operation.");
        }
      } catch (InterruptedException e) {
        log.warn("Interrupted while shutting down the async job executor. ", e);
      }
  
      executorService = null;
    }
  }

  /** Starts the acquisition thread */
  protected void startJobAcquisitionThread() {
    if (asyncJobAcquisitionThread == null) {
      asyncJobAcquisitionThread = new Thread(asyncJobsDueRunnable);
    }
    asyncJobAcquisitionThread.start();
  }

  protected void startTimerAcquisitionThread() {
    if (timerJobAcquisitionThread == null) {
      timerJobAcquisitionThread = new Thread(timerJobRunnable);
    }
    timerJobAcquisitionThread.start();
  }

  /** Stops the acquisition thread */
  protected void stopJobAcquisitionThread() {
    if (asyncJobAcquisitionThread != null) {
      try {
        asyncJobAcquisitionThread.join();
      } catch (InterruptedException e) {
        log.warn("Interrupted while waiting for the async job acquisition thread to terminate", e);
      }
      asyncJobAcquisitionThread = null;
    }
  }

  protected void stopTimerAcquisitionThread() {
    if (timerJobAcquisitionThread != null) {
      try {
        timerJobAcquisitionThread.join();
      } catch (InterruptedException e) {
        log.warn("Interrupted while waiting for the timer job acquisition thread to terminate", e);
      }
      timerJobAcquisitionThread = null;
    }
  }
  
  /** Starts the reset expired jobs thread */
  protected void startResetExpiredJobsThread() {
    if (resetExpiredJobThread == null) {
      resetExpiredJobThread = new Thread(resetExpiredJobsRunnable);
    }
    resetExpiredJobThread.start();
  }
  
  /** Stops the reset expired jobs thread */
  protected void stopResetExpiredJobsThread() {
    if (resetExpiredJobThread != null) {
      try {
        resetExpiredJobThread.join();
      } catch (InterruptedException e) {
        log.warn("Interrupted while waiting for the reset expired jobs thread to terminate", e);
      }
  
      resetExpiredJobThread = null;
    }
  }

  public void applyConfig(ProcessEngineConfigurationImpl processEngineConfiguration){
    isMessageQueueMode = processEngineConfiguration.isAsyncExecutorIsMessageQueueMode();
    applyThreadPoolConfig(processEngineConfiguration);
    applyQueueConfig(processEngineConfiguration);

    defaultTimerJobAcquireWaitTimeInMillis = processEngineConfiguration.getAsyncExecutorDefaultTimerJobAcquireWaitTime();
    defaultAsyncJobAcquireWaitTimeInMillis = processEngineConfiguration.getAsyncExecutorDefaultAsyncJobAcquireWaitTime();

    applyLockConfig(processEngineConfiguration);

    resetExpiredJobsInterval = processEngineConfiguration.getAsyncExecutorResetExpiredJobsInterval();
    resetExpiredJobsPageSize = processEngineConfiguration.getAsyncExecutorResetExpiredJobsPageSize();

    secondsToWaitOnShutdown = processEngineConfiguration.getAsyncExecutorSecondsToWaitOnShutdown();
    
    maxAsyncJobsDuePerAcquisition = processEngineConfiguration.getAsyncExecutorMaxAsyncJobsDuePerAcquisition();
    maxTimerJobsPerAcquisition = processEngineConfiguration.getAsyncExecutorMaxTimerJobsPerAcquisition();

    retryWaitTimeInMillis = processEngineConfiguration.getAsyncFailedJobWaitTime();
  }

  private void applyLockConfig(ProcessEngineConfigurationImpl processEngineConfiguration) {
    timerLockTimeInMillis = processEngineConfiguration.getAsyncExecutorTimerLockTimeInMillis();
    asyncJobLockTimeInMillis = processEngineConfiguration.getAsyncExecutorAsyncJobLockTimeInMillis();
    if (processEngineConfiguration.getAsyncExecutorLockOwner() != null) {
      lockOwner = processEngineConfiguration.getAsyncExecutorLockOwner();
    }
  }

  private void applyQueueConfig(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (processEngineConfiguration.getAsyncExecutorThreadPoolQueue() != null) {
      threadPoolQueue = processEngineConfiguration.getAsyncExecutorThreadPoolQueue();
    }
    queueSize = processEngineConfiguration.getAsyncExecutorThreadPoolQueueSize();
    defaultQueueSizeFullWaitTime = processEngineConfiguration.getAsyncExecutorDefaultQueueSizeFullWaitTime();
  }

  private void applyThreadPoolConfig(ProcessEngineConfigurationImpl processEngineConfiguration) {
    corePoolSize = processEngineConfiguration.getAsyncExecutorCorePoolSize();
    maxPoolSize = processEngineConfiguration.getAsyncExecutorMaxPoolSize();
    keepAliveTime = processEngineConfiguration.getAsyncExecutorThreadKeepAliveTime();
  }

  /* getters and setters */
  
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }


  public Thread getTimerJobAcquisitionThread() {
    return timerJobAcquisitionThread;
  }

  public void setTimerJobAcquisitionThread(Thread timerJobAcquisitionThread) {
    this.timerJobAcquisitionThread = timerJobAcquisitionThread;
  }

  public Thread getAsyncJobAcquisitionThread() {
    return asyncJobAcquisitionThread;
  }

  public void setAsyncJobAcquisitionThread(Thread asyncJobAcquisitionThread) {
    this.asyncJobAcquisitionThread = asyncJobAcquisitionThread;
  }

  public Thread getResetExpiredJobThread() {
    return resetExpiredJobThread;
  }

  public void setResetExpiredJobThread(Thread resetExpiredJobThread) {
    this.resetExpiredJobThread = resetExpiredJobThread;
  }

  public boolean isAutoActivate() {
    return isAutoActivate;
  }

  public void setAutoActivate(boolean isAutoActivate) {
    this.isAutoActivate = isAutoActivate;
  }

  public boolean isActive() {
    return isActive;
  }
  
  public boolean isMessageQueueMode() {
    return isMessageQueueMode;
  }

  public void setMessageQueueMode(boolean isMessageQueueMode) {
    this.isMessageQueueMode = isMessageQueueMode;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public long getKeepAliveTime() {
    return keepAliveTime;
  }

  public void setKeepAliveTime(long keepAliveTime) {
    this.keepAliveTime = keepAliveTime;
  }

  public long getSecondsToWaitOnShutdown() {
    return secondsToWaitOnShutdown;
  }

  public void setSecondsToWaitOnShutdown(long secondsToWaitOnShutdown) {
    this.secondsToWaitOnShutdown = secondsToWaitOnShutdown;
  }

  public BlockingQueue<Runnable> getThreadPoolQueue() {
    return threadPoolQueue;
  }

  public void setThreadPoolQueue(BlockingQueue<Runnable> threadPoolQueue) {
    this.threadPoolQueue = threadPoolQueue;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public int getTimerLockTimeInMillis() {
    return timerLockTimeInMillis;
  }

  public void setTimerLockTimeInMillis(int timerLockTimeInMillis) {
    this.timerLockTimeInMillis = timerLockTimeInMillis;
  }

  public int getAsyncJobLockTimeInMillis() {
    return asyncJobLockTimeInMillis;
  }

  public void setAsyncJobLockTimeInMillis(int asyncJobLockTimeInMillis) {
    this.asyncJobLockTimeInMillis = asyncJobLockTimeInMillis;
  }

  public int getMaxTimerJobsPerAcquisition() {
    return maxTimerJobsPerAcquisition;
  }

  public void setMaxTimerJobsPerAcquisition(int maxTimerJobsPerAcquisition) {
    this.maxTimerJobsPerAcquisition = maxTimerJobsPerAcquisition;
  }

  public int getMaxAsyncJobsDuePerAcquisition() {
    return maxAsyncJobsDuePerAcquisition;
  }

  public void setMaxAsyncJobsDuePerAcquisition(int maxAsyncJobsDuePerAcquisition) {
    this.maxAsyncJobsDuePerAcquisition = maxAsyncJobsDuePerAcquisition;
  }

  public int getDefaultTimerJobAcquireWaitTimeInMillis() {
    return defaultTimerJobAcquireWaitTimeInMillis;
  }

  public void setDefaultTimerJobAcquireWaitTimeInMillis(int defaultTimerJobAcquireWaitTimeInMillis) {
    this.defaultTimerJobAcquireWaitTimeInMillis = defaultTimerJobAcquireWaitTimeInMillis;
  }

  public int getDefaultAsyncJobAcquireWaitTimeInMillis() {
    return defaultAsyncJobAcquireWaitTimeInMillis;
  }

  public void setDefaultAsyncJobAcquireWaitTimeInMillis(int defaultAsyncJobAcquireWaitTimeInMillis) {
    this.defaultAsyncJobAcquireWaitTimeInMillis = defaultAsyncJobAcquireWaitTimeInMillis;
  }

  public void setTimerJobRunnable(AcquireTimerJobsRunnable timerJobRunnable) {
    this.timerJobRunnable = timerJobRunnable;
  }
  
  public int getDefaultQueueSizeFullWaitTimeInMillis() {
    return defaultQueueSizeFullWaitTime;
  }

  public void setDefaultQueueSizeFullWaitTimeInMillis(int defaultQueueSizeFullWaitTime) {
    this.defaultQueueSizeFullWaitTime = defaultQueueSizeFullWaitTime;
  }

  public void setAsyncJobsDueRunnable(AcquireAsyncJobsDueRunnable asyncJobsDueRunnable) {
    this.asyncJobsDueRunnable = asyncJobsDueRunnable;
  }
  
  public void setResetExpiredJobsRunnable(ResetExpiredJobsRunnable resetExpiredJobsRunnable) {
    this.resetExpiredJobsRunnable = resetExpiredJobsRunnable;
  }

  public int getRetryWaitTimeInMillis() {
		return retryWaitTimeInMillis;
	}

	public void setRetryWaitTimeInMillis(int retryWaitTimeInMillis) {
		this.retryWaitTimeInMillis = retryWaitTimeInMillis;
	}
	
  public int getResetExpiredJobsInterval() {
    return resetExpiredJobsInterval;
  }

  public void setResetExpiredJobsInterval(int resetExpiredJobsInterval) {
    this.resetExpiredJobsInterval = resetExpiredJobsInterval;
  }
  
  public int getResetExpiredJobsPageSize() {
    return resetExpiredJobsPageSize;
  }

  public void setResetExpiredJobsPageSize(int resetExpiredJobsPageSize) {
    this.resetExpiredJobsPageSize = resetExpiredJobsPageSize;
  }

  public ExecuteAsyncRunnableFactory getExecuteAsyncRunnableFactory() {
    return executeAsyncRunnableFactory;
  }

  public void setExecuteAsyncRunnableFactory(ExecuteAsyncRunnableFactory executeAsyncRunnableFactory) {
    this.executeAsyncRunnableFactory = executeAsyncRunnableFactory;
  }

}
