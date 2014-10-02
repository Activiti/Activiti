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
package org.activiti.engine.impl.jobexecutor;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>This is a simple implementation of the {@link JobExecutor} using self-managed
 * threads for performing background work.</p>
 * 
 * <p>This implementation uses a {@link ThreadPoolExecutor} backed by a queue to which
 * work is submitted.</p>
 * 
 * <p><em>NOTE: use this class in environments in which self-management of threads 
 * is permitted. Consider using a different thread-management strategy in 
 * J(2)EE-Environments.</em></p>
 * 
 * @author Daniel Meyer
 */
public class DefaultJobExecutor extends JobExecutor {
  
  private static Logger log = LoggerFactory.getLogger(DefaultJobExecutor.class);
  
  protected int queueSize = 3;
  protected int corePoolSize = 3;
  protected int maxPoolSize = 10;
  protected long keepAliveTime = 0L;

  protected BlockingQueue<Runnable> threadPoolQueue;
  protected ThreadPoolExecutor threadPoolExecutor;
    
  protected void startExecutingJobs() {
    if (threadPoolQueue==null) {
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    if (threadPoolExecutor==null) {
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue);      
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }
    startJobAcquisitionThread(); 
  }
    
  protected void stopExecutingJobs() {
    stopJobAcquisitionThread();
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if(!threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
        log.warn("Timeout during shutdown of job executor. "
                + "The current running jobs could not end within 60 seconds after shutdown operation.");        
      }              
    } catch (InterruptedException e) {
      log.warn("Interrupted while shutting down the job executor. ", e);
    }

    threadPoolExecutor = null;
  }
  
  public void executeJobs(List<String> jobIds) {
    try {
      threadPoolExecutor.execute(new ExecuteJobsRunnable(this, jobIds));
    } catch (RejectedExecutionException e) {
      rejectedJobsHandler.jobsRejected(this, jobIds);
    }
  }
  
  // getters and setters ////////////////////////////////////////////////////// 
  
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
    
}

