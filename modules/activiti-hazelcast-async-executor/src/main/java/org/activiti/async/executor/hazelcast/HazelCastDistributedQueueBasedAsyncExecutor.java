package org.activiti.async.executor.hazelcast;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.hazelcast.monitor.LocalQueueStats;

/**
 * Implementation of the Activiti {@link AsyncExecutor} using a distributed queue where the jobs
 * to be executed are put on. One of the distributed nodes will take the job off the queue, 
 * and hand it off the local thread pool.
 * 
 * Needs a config file (hazelcast.xml on the classpath) that defines a queue with the name 'activiti':
 * 
 * for example:
 * 
 * <hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.3.xsd"
           xmlns="http://www.hazelcast.com/schema/config"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <group>
       ...
    </group>
    
    <network>
       ...
    </network>
    
    
     <queue name="activiti">
        <!--
            Maximum size of the queue. When a JVM's local queue size reaches the maximum,
            all put/offer operations will get blocked until the queue size
            of the JVM goes down below the maximum.
            Any integer between 0 and Integer.MAX_VALUE. 0 means
            Integer.MAX_VALUE. Default is 0.
        -->
        <max-size>1024</max-size>
        <!--
            Number of backups. If 1 is set as the backup-count for example,
            then all entries of the map will be copied to another JVM for
            fail-safety. 0 means no backup.
        -->
        <backup-count>0</backup-count>

        <!--
            Number of async backups. 0 means no backup.
        -->
        <async-backup-count>0</async-backup-count>

        <empty-queue-ttl>-1</empty-queue-ttl>
    </queue>


</hazelcast>
 * 
 * @author Joram Barrez
 */
public class HazelCastDistributedQueueBasedAsyncExecutor implements AsyncExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(HazelCastDistributedQueueBasedAsyncExecutor.class);
	
	private static final String QUEUE_NAME = "activiti";
	
	// Injecteable
	protected boolean isAutoActivate;
	protected CommandExecutor commandExecutor;
	protected int maxTimerJobsPerAcquisition = 1;
	protected int maxAsyncJobsDuePerAcquisition = 1;
	protected int defaultTimerJobAcquireWaitTimeInMillis = 10 * 1000;
	protected int defaultAsyncJobAcquireWaitTimeInMillis = 10 * 1000;
	  
	protected String lockOwner = UUID.randomUUID().toString();
	protected int timerLockTimeInMillis = 5 * 60 * 1000;
	protected int asyncJobLockTimeInMillis = 5 * 60 * 1000;
	
	 /** The minimal number of threads that are kept alive in the threadpool for job execution */
  protected int corePoolSize = 2;
  
  /** The maximum number of threads that are kept alive in the threadpool for job execution */
  protected int maxPoolSize = 10;
  
  /** 
   * The time (in milliseconds) a thread used for job execution must be kept alive before it is
   * destroyed. Default setting is 0. Having a non-default setting of 0 takes resources,
   * but in the case of many job executions it avoids creating new threads all the time. 
   */
  protected long keepAliveTime = 60000L;

	/** The size of the queue on which jobs to be executed are placed */
  protected int queueSize = 100;
  
  /** The time (in seconds) that is waited to gracefully shut down the threadpool used for job execution */
  protected long secondsToWaitOnShutdown = 60L;
	
	// Runtime
	protected boolean isActive;
	protected HazelcastInstance hazelcastInstance;
	protected IQueue<JobEntity> jobQueue;
	
	protected Thread jobQueueListenerThread;
	protected BlockingQueue<Runnable> threadPoolQueue;
	protected ExecutorService executorService;
	
	@Override
	public void start() {
		if (isActive) {
			return;
		}

		logger.info("Starting up the Hazelcast async job executor [{}].", getClass().getName());

		hazelcastInstance = Hazelcast.newHazelcastInstance();
		jobQueue = hazelcastInstance.getQueue(QUEUE_NAME);

		threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue);
		threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executorService = threadPoolExecutor;
		
		isActive = true;
		initJobQueueListener();
	}
	
	protected void initJobQueueListener() {
		jobQueueListenerThread = new Thread(new Runnable() {
			
			public void run() {
				while (isActive) {
					JobEntity job = null;
					try {
				    job = jobQueue.take(); // Blocking
			    } catch (InterruptedException e1) {
				    // Do nothing, this can happen when shutting down
			    }
					
					if (job != null) {
						executorService.execute(new ExecuteAsyncRunnable(job, commandExecutor));
					}
				}
			}
			
		});
		jobQueueListenerThread.start();
	}

	@Override
  public void shutdown() {
		
		// Shut down local execution service
		try {
			executorService.shutdown();
	    executorService.awaitTermination(secondsToWaitOnShutdown, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
    	logger.warn("Exception while waiting for executor service shutdown", e);
    }
		
		// Shut down listener thread
		isActive = false;
		
		LocalQueueStats localQueueStats = jobQueue.getLocalQueueStats();
		logger.info("This async job executor has processed " + localQueueStats.getPollOperationCount());

		// Shut down hazelcast
		try {
			hazelcastInstance.shutdown();
		} catch (HazelcastInstanceNotActiveException e) {
			// Nothing to do
		}
  }
	
	@Override
  public void executeAsyncJob(JobEntity job) {
		try {
	    jobQueue.put(job);
    } catch (InterruptedException e) {
	    // Nothing to do about it, can happen at shutdown for example
    }
  }
	
	@Override
  public boolean isActive() {
		return isActive;
  }
	
	@Override
	public CommandExecutor getCommandExecutor() {
		return commandExecutor;
	}


	@Override
  public void setCommandExecutor(CommandExecutor commandExecutor) {
	  this.commandExecutor = commandExecutor;
  }

	@Override
  public boolean isAutoActivate() {
		return isAutoActivate;
  }

	@Override
  public void setAutoActivate(boolean isAutoActivate) {
		this.isAutoActivate = isAutoActivate;
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

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public long getSecondsToWaitOnShutdown() {
		return secondsToWaitOnShutdown;
	}

	public void setSecondsToWaitOnShutdown(long secondsToWaitOnShutdown) {
		this.secondsToWaitOnShutdown = secondsToWaitOnShutdown;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
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

	public void setDefaultTimerJobAcquireWaitTimeInMillis(
	    int defaultTimerJobAcquireWaitTimeInMillis) {
		this.defaultTimerJobAcquireWaitTimeInMillis = defaultTimerJobAcquireWaitTimeInMillis;
	}

	public int getDefaultAsyncJobAcquireWaitTimeInMillis() {
		return defaultAsyncJobAcquireWaitTimeInMillis;
	}

	public void setDefaultAsyncJobAcquireWaitTimeInMillis(
	    int defaultAsyncJobAcquireWaitTimeInMillis) {
		this.defaultAsyncJobAcquireWaitTimeInMillis = defaultAsyncJobAcquireWaitTimeInMillis;
	}
	
}
