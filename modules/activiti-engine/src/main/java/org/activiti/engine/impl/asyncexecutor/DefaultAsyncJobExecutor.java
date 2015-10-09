package org.activiti.engine.impl.asyncexecutor;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DefaultAsyncJobExecutor implements AsyncExecutor {

private static Logger log = LoggerFactory.getLogger(DefaultAsyncJobExecutor.class);
  
  /** The minimal number of threads that are kept alive in the threadpool for job execution */
  protected int corePoolSize = 2;
  
  /** The maximum number of threads that are kept alive in the threadpool for job execution */
  protected int maxPoolSize = 10;
  
  /** 
   * The time (in milliseconds) a thread used for job execution must be kept alive before it is
   * destroyed. Default setting is 0. Having a non-default setting of 0 takes resources,
   * but in the case of many job executions it avoids creating new threads all the time. 
   */
  protected long keepAliveTime = 5000L;

	/** The size of the queue on which jobs to be executed are placed */
  protected int queueSize = 100;
  
  /** The queue used for job execution work */
  protected BlockingQueue<Runnable> threadPoolQueue;
  
  /** The executor service used for job execution */
  protected ExecutorService executorService;
  
  /** The time (in seconds) that is waited to gracefully shut down the threadpool used for job execution */
  protected long secondsToWaitOnShutdown = 60L;
  
  protected Thread timerJobAcquisitionThread;
  protected Thread asyncJobAcquisitionThread;
  protected AcquireTimerJobsRunnable timerJobRunnable;
  protected AcquireAsyncJobsDueRunnable asyncJobsDueRunnable;
  
  protected ExecuteAsyncRunnableFactory executeAsyncRunnableFactory;
  
  protected boolean isAutoActivate = false;
  protected boolean isActive = false;
  
  protected int maxTimerJobsPerAcquisition = 1;
  protected int maxAsyncJobsDuePerAcquisition = 1;
  protected int defaultTimerJobAcquireWaitTimeInMillis = 10 * 1000;
  protected int defaultAsyncJobAcquireWaitTimeInMillis = 10 * 1000;
  
  protected String lockOwner = UUID.randomUUID().toString();
  protected int timerLockTimeInMillis = 5 * 60 * 1000;
  protected int asyncJobLockTimeInMillis = 5 * 60 * 1000;
  protected int retryWaitTimeInMillis = 500;
  
  // Job queue used when async executor is not yet started and jobs are already added.
  // This is mainly used for testing purpose.
  protected LinkedList<JobEntity> temporaryJobQueue = new LinkedList<JobEntity>();
  
  protected CommandExecutor commandExecutor;
  
  public void executeAsyncJob(JobEntity job) {
  	Runnable runnable = null;
    if (isActive) {
    	if (executeAsyncRunnableFactory == null) {
    		runnable = new ExecuteAsyncRunnable(job, commandExecutor);
    	} else {
    		runnable = executeAsyncRunnableFactory.createExecuteAsyncRunnable(job, commandExecutor);
    	}
    	executorService.execute(runnable);
    } else {
      temporaryJobQueue.add(job);
    }
  }
  
  /** Starts the async executor */
  public void start() {
    if (isActive) {
      return;
    }
    
    log.info("Starting up the default async job executor [{}].", getClass().getName());
    if (timerJobRunnable == null) {
      timerJobRunnable = new AcquireTimerJobsRunnable(this);
    }
    if (asyncJobsDueRunnable == null) {
      asyncJobsDueRunnable = new AcquireAsyncJobsDueRunnable(this);
    }
    startExecutingAsyncJobs();
    
    isActive = true;
        
    while (temporaryJobQueue.isEmpty() == false) {
    	JobEntity job = temporaryJobQueue.pop();
      executeAsyncJob(job);
    }
    isActive = true;
  }
  
  /** Shuts down the whole job executor */
  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the default async job executor [{}].", getClass().getName());
    timerJobRunnable.stop();
    asyncJobsDueRunnable.stop();
    stopExecutingAsyncJobs();
    
    timerJobRunnable = null;
    asyncJobsDueRunnable = null;
    isActive = false;
  }

  protected void startExecutingAsyncJobs() {
    if (threadPoolQueue==null) {
    	log.info("Creating thread pool queue of size {}", queueSize);
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    
    if (executorService==null) {
    	log.info("Creating executor service with corePoolSize {}, maxPoolSize {} and keepAliveTime {}",
    			corePoolSize, maxPoolSize, keepAliveTime);
    	
    	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue);      
    	threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    	executorService = threadPoolExecutor;
    	
    }
    
    startJobAcquisitionThread();
  }
    
  protected void stopExecutingAsyncJobs() {
    stopJobAcquisitionThread();
    
    // Ask the thread pool to finish and exit
  	executorService.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if(!executorService.awaitTermination(secondsToWaitOnShutdown, TimeUnit.SECONDS)) {
        log.warn("Timeout during shutdown of async job executor. "
            + "The current running jobs could not end within " 
        		+ secondsToWaitOnShutdown + " seconds after shutdown operation.");        
      }              
    } catch (InterruptedException e) {
      log.warn("Interrupted while shutting down the async job executor. ", e);
    }

    executorService = null;
  }
  
  /** Starts the acquisition thread */
  protected void startJobAcquisitionThread() {
    if (timerJobAcquisitionThread == null) {
      timerJobAcquisitionThread = new Thread(timerJobRunnable);
    }
    timerJobAcquisitionThread.start();
    
    if (asyncJobAcquisitionThread == null) {
      asyncJobAcquisitionThread = new Thread(asyncJobsDueRunnable);
    }
    asyncJobAcquisitionThread.start();
  }
  
  /** Stops the acquisition thread */
  protected void stopJobAcquisitionThread() {
    try {
      timerJobAcquisitionThread.join();
    } catch (InterruptedException e) {
      log.warn("Interrupted while waiting for the timer job acquisition thread to terminate", e);
    }
    
    try {
      asyncJobAcquisitionThread.join();
    } catch (InterruptedException e) {
      log.warn("Interrupted while waiting for the async job acquisition thread to terminate", e);
    } 
    
    timerJobAcquisitionThread = null;
    asyncJobAcquisitionThread = null;
  }
	
  /* getters and setters */ 
  
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
  
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
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

  public void setAsyncJobsDueRunnable(AcquireAsyncJobsDueRunnable asyncJobsDueRunnable) {
    this.asyncJobsDueRunnable = asyncJobsDueRunnable;
  }

	public int getRetryWaitTimeInMillis() {
		return retryWaitTimeInMillis;
	}

	public void setRetryWaitTimeInMillis(int retryWaitTimeInMillis) {
		this.retryWaitTimeInMillis = retryWaitTimeInMillis;
	}

	public ExecuteAsyncRunnableFactory getExecuteAsyncRunnableFactory() {
		return executeAsyncRunnableFactory;
	}

	public void setExecuteAsyncRunnableFactory(ExecuteAsyncRunnableFactory executeAsyncRunnableFactory) {
		this.executeAsyncRunnableFactory = executeAsyncRunnableFactory;
	}

}
