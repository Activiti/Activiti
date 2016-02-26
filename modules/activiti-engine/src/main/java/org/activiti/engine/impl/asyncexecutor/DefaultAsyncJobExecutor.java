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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DefaultAsyncJobExecutor extends AbstractAsyncJobExecutor {

  private static Logger log = LoggerFactory.getLogger(DefaultAsyncJobExecutor.class);
  
  /** The minimal number of threads that are kept alive in the threadpool for job execution */
  protected int corePoolSize = 2;
  
  /** The maximum number of threads that are kept alive in the threadpool for job execution */
  protected int maxPoolSize = 10;
  
  /** The size of the queue on which jobs to be executed are placed */
  protected int queueSize = 100;
  
  /** The queue used for job execution work */
  protected BlockingQueue<Runnable> threadPoolQueue;
  
  /** The executor service used for job execution */
  protected ExecutorService executorService;
  
  /** The time (in seconds) that is waited to gracefully shut down the threadpool used for job execution */
  protected long secondsToWaitOnShutdown = 60L;
  
  protected boolean executeAsyncJob(Runnable runnable) {
    try {
      executorService.execute(runnable);
      return true;
    } catch (RejectedExecutionException e) {
      // When a RejectedExecutionException is caught, this means that the queue for holding the jobs 
      // that are to be executed is full and can't store more.
      // Return false so the job can be unlocked and (if wanted) the acquiring can be throttled.
      return false;
    }
  }
  
  protected Runnable createRunnableForJob(final JobEntity job) {
    return executeAsyncRunnableFactory.createExecuteAsyncRunnable(job, commandExecutor);
  }
 
  protected void unlockJob(final JobEntity job, CommandContext commandContext) {
    commandContext.getJobEntityManager().unacquireJob(job.getId());
  }
  
  protected void startExecutingAsyncJobs() {
    if (threadPoolQueue==null) {
      log.info("Creating thread pool queue of size {}", queueSize);
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    
    if (executorService==null) {
      log.info("Creating executor service with corePoolSize {}, maxPoolSize {} and keepAliveTime {}",
          corePoolSize, maxPoolSize, keepAliveTime);
      
      executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue);      
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
}
