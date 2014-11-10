package org.activiti.engine.impl.jobexecutor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JobExecutor, the next generation.
 * 
 * @author Joram Barrez
 */
public class LockFreeJobExecutor extends JobExecutor {

private static Logger log = LoggerFactory.getLogger(LockFreeJobExecutor.class);
  
  /** The minimal number of threads that are kept alive in the threadpool for job execution */
  protected int corePoolSize = 2;
  
  /** The maximum number of threads that are kept alive in the threadpool for job execution */
  protected int maxPoolSize = 10;
  
  /** 
   * The time (in milliseconds) a thread used for job execution must be kept alive before it is
   * destroyed. Default setting is 0. Having a non-default setting of 0 takes resources,
   * but in the case of many job executions it avoids creating new threads all the time. 
   */
  protected long keepAliveTime = 0L;
  
  /** Used to avoid putting the same job twice on the queue */
  protected Set<String> currentlyProcessedJobs;
  
  /**
   * When a job is removed from the currentlyProcessedJobs (in the ExecuteJobsRunnable), there can be a 
   * slight window of overlap: the jobs are already fetched in memory, but the removal happened just before.
   * At that point, the acquirement logic will try to put them on the queue again 
   * (because they were read from the database AND not found in the 'currentlyProcessedJobs' set,
   * hence they are thought to be real, unprocessed jobs).
   * 
   * To solve this, we keep a LRU cache of the last deleted entries. 
   * The size of this cache is equal to the 'currentlyProcessedJobs' size or the jobFetchBatchSize
   * (the largest one of the two). Making it larger has no point, as with this 
   * size the window of overlap is covered 
   */
  protected Map<String, JobEntity> recentlyRemovedJobs;

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
  
  /** The runnable responsible for fetching the jobs and put on the queue */
  protected LockFreeAcquireJobsRunnable lockFreeAcquireJobsRunnable;
  
  public LockFreeJobExecutor() {
  	
  	// We want the runnable instance locally here, hence both the 'this.' and the setter (from JobExecutor)
  	this.lockFreeAcquireJobsRunnable = new LockFreeAcquireJobsRunnable(this);
  	setAcquireJobsRunnable(this.lockFreeAcquireJobsRunnable);
  	
  	initCurrentlyProcessedDataStructures();
  }

  protected void startExecutingJobs() {
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
    
  protected void stopExecutingJobs() {
    stopJobAcquisitionThread();
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if(!threadPoolExecutor.awaitTermination(secondsToWaitOnShutdown, TimeUnit.SECONDS)) {
        log.warn("Timeout during shutdown of job executor. "
            + "The current running jobs could not end within " 
        		+ secondsToWaitOnShutdown + " seconds after shutdown operation.");        
      }              
    } catch (InterruptedException e) {
      log.warn("Interrupted while shutting down the job executor. ", e);
    }

    threadPoolExecutor = null;
  }
  
  public void executeJobs(List<String> jobIds) {
  	// Not doing anything in this implementation, the acquire runnable puts work on the threadpool
  }
  
  @Override
  protected void startJobAcquisitionThread() {
  	if (jobAcquisitionThread == null) {
  		if (this.acquireJobsRunnable == null) {
  			jobAcquisitionThread = new Thread(new LockFreeAcquireJobsRunnable(this));
  		} else {
  			jobAcquisitionThread = new Thread(this.acquireJobsRunnable);
  		}
		}
		jobAcquisitionThread.start();
  }
  
  
  /* Currently processed / recently removed data structure init + methods */
	
	protected void initCurrentlyProcessedDataStructures() {
		final int size = Math.max(getQueueSize(), getJobFetchBatchSize());
		this.currentlyProcessedJobs = Collections.synchronizedSet(new HashSet<String>(size));
		
		// +1 is needed, because the entry is inserted first, before it is removed, 
		// 0.75 is the default (see javadocs) 
		// true will keep the 'access-order', which is needed to have a real LRU cache
		
		final int recentlyRemovesJobsMaxSize = (size*2) +1; // Needs to be at least *2 to contain a whole new batch of fetched jobs
		this.recentlyRemovedJobs = Collections
		    .synchronizedMap(new LinkedHashMap<String, JobEntity>(recentlyRemovesJobsMaxSize, 0.75f, true) {
			    private static final long serialVersionUID = 1L;

			    protected boolean removeEldestEntry(Map.Entry<String, JobEntity> eldest) {
				    return size() > recentlyRemovesJobsMaxSize;
			    }

		    });
	}
	
	public void jobScheduledForExecution(JobEntity job) {
		currentlyProcessedJobs.add(job.getId());
	}
	
	public boolean isJobScheduledForExecution(JobEntity job) {
		boolean recentlyExecuted = currentlyProcessedJobs.contains(job.getId());
		if (recentlyExecuted) {
			return true;
		}
		return recentlyRemovedJobs.containsKey(job.getId());
	}
	
	public void jobDone(JobEntity job) {
		// DO NOT swap these two lines, the order is very crucial to working correctly!
		this.recentlyRemovedJobs.put(job.getId(), job);
		currentlyProcessedJobs.remove(job.getId());
	}
  
  /* getters and setters */ 
  
  public int getQueueSize() {
    return queueSize;
  }
  
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
    initCurrentlyProcessedDataStructures();
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
		initCurrentlyProcessedDataStructures();
	}
	
	public Set<String> getCurrentlyProcessedJobs() {
		return currentlyProcessedJobs;
	}

	public void setCurrentlyProcessedJobs(Set<String> currentlyProcessedJobs) {
		this.currentlyProcessedJobs = currentlyProcessedJobs;
	}

	public Map<String, JobEntity> getRecentlyRemovedJobs() {
		return recentlyRemovedJobs;
	}

	public void setRecentlyRemovedJobs(Map<String, JobEntity> recentlyRemovedJobs) {
		this.recentlyRemovedJobs = recentlyRemovedJobs;
	}
	
}
