package org.activiti.engine.impl.asyncexecutor;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default async executor, the next generation.
 * 
 * @author Job Barrez
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
  
  /** The number of jobs fetched in one query after they are already acquired */
  protected int jobFetchBatchSize = 100;
  
  /** The queue used for job execution work */
  protected BlockingQueue<Runnable> threadPoolQueue;
  
  /** The thread pool used for job execution */
  protected ThreadPoolExecutor threadPoolExecutor;
  
  /** The time (in seconds) that is waited to gracefully shut down the threadpool used for job execution */
  protected long secondsToWaitOnShutdown = 60L;
  
  protected Thread jobAcquisitionThread;
  protected AcquireAsyncJobsDueRunnable asyncJobsDueRunnable;
  
  protected boolean isAutoActivate = false;
  protected boolean isActive = false;
  
  // Job queue used when async executor is not yet started and jobs are already added.
  // This is mainly used for testing purpose.
  protected LinkedList<JobEntity> temporaryJobQueue = new LinkedList<JobEntity>();
  
  protected CommandExecutor commandExecutor;
  
  public void executeAsyncJob(JobEntity job) {
    if (isActive) {
      threadPoolExecutor.execute(new ExecuteAsyncRunnable(job, commandExecutor));
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
    if (asyncJobsDueRunnable == null) {
      asyncJobsDueRunnable = new AcquireAsyncJobsDueRunnable(this);
    }
    startExecutingAsyncJobs();
    isActive = true;
    
    while (temporaryJobQueue.isEmpty() == false) {
      JobEntity job = temporaryJobQueue.pop();
      executeAsyncJob(job);
    }
  }
  
  /** Shuts down the whole job executor */
  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the default async job executor [{}].", getClass().getName());
    asyncJobsDueRunnable.stop();
    stopExecutingAyncJobs();
    asyncJobsDueRunnable = null;
    isActive = false;
  }

  protected void startExecutingAsyncJobs() {
    if (threadPoolQueue==null) {
    	log.info("Creating thread pool queue of size {}", queueSize);
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    
    if (threadPoolExecutor==null) {
    	log.info("Creating thread pool executor with corePoolSize {}, maxPoolSize {} and keepAliveTime {}",
    			corePoolSize, maxPoolSize, keepAliveTime);
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue);      
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    startJobAcquisitionThread();
  }
    
  protected void stopExecutingAyncJobs() {
    stopJobAcquisitionThread();
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if(!threadPoolExecutor.awaitTermination(secondsToWaitOnShutdown, TimeUnit.SECONDS)) {
        log.warn("Timeout during shutdown of async job executor. "
            + "The current running jobs could not end within " 
        		+ secondsToWaitOnShutdown + " seconds after shutdown operation.");        
      }              
    } catch (InterruptedException e) {
      log.warn("Interrupted while shutting down the async job executor. ", e);
    }

    threadPoolExecutor = null;
  }
  
  /** Starts the acquisition thread */
  protected void startJobAcquisitionThread() {
    if (jobAcquisitionThread == null) {
      jobAcquisitionThread = new Thread(asyncJobsDueRunnable);
    }
    jobAcquisitionThread.start();
  }
  
  /** Stops the acquisition thread */
  protected void stopJobAcquisitionThread() {
    try {
      jobAcquisitionThread.join();
    } catch (InterruptedException e) {
      log.warn("Interrupted while waiting for the job Acquisition thread to terminate", e);
    } 
    jobAcquisitionThread = null;
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

  public ThreadPoolExecutor getThreadPoolExecutor() {
    return threadPoolExecutor;
  }
  
  public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;
  }

	public int getJobFetchBatchSize() {
		return jobFetchBatchSize;
	}

	public void setJobFetchBatchSize(int jobFetchBatchSize) {
		this.jobFetchBatchSize = jobFetchBatchSize;
	}
}
