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
package org.activiti.impl.jobexecutor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.activiti.impl.interceptor.CommandExecutor;

/**
 * Manager class in charge of all background / asynchronous
 *  processing.
 * You should generally only have one of these per Activiti
 *  instance in a JVM. In clustered situations, you can have
 *  multiple of these running against the same queue +
 *  pending job list.
 * Uses a {@link ThreadPoolExecutor} internally.
 */
public class JobExecutor {
  
  private static Logger log = Logger.getLogger(JobExecutor.class.getName());

  protected final CommandExecutor commandExecutor;

  protected int maxJobsPerAcquisition = 3;
  protected int waitTimeInMillis = 5 * 1000;
  protected String lockOwner = UUID.randomUUID().toString();
  protected int lockTimeInMillis = 5 * 60 * 1000;
  protected int queueSize = 5;
  protected int corePoolSize = 3;
  protected int maxPoolSize = 10;

  protected JobAcquisitionThread jobAcquisitionThread;
  protected BlockingQueue<Runnable> threadPoolQueue;
  protected ThreadPoolExecutor threadPoolExecutor;
  
  protected boolean isActive = false;

  public JobExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public synchronized void start() {
    if(isActive) {
      // Already started, nothing to do
      log.info("Ignoring duplicate JobExecutor start invocation");
      return;
    } else {
      isActive = true;
      
      if (jobAcquisitionThread==null) {
        jobAcquisitionThread = new JobAcquisitionThread(this);
      }
      if (threadPoolQueue==null) {
        threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
      }
      if (threadPoolExecutor==null) {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);
      }
      
      // Create our pending jobs fetcher
      log.fine("JobExecutor is starting the JobAcquisitionThread");
      jobAcquisitionThread.start();
    }
  }
  
  public void shutdown() {
    if(!isActive) {
      log.info("Ignoring request to shut down non-active JobExecutor");
      return;
    }
    
    log.info("Shutting down the JobExecutor");
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();
    
    // Close the pending jobs task
    jobAcquisitionThread.shutdown();
    
    isActive = false;

    // Clear references
    threadPoolExecutor = null;
    jobAcquisitionThread = null;
  }
  
  
  /**
   * Used to hint that new work exists on the
   *  queue, and that the {@link JobAcquisitionThread}
   *  should probably re-check for jobs.
   */
  public void jobWasAdded() {
    if ( isActive 
         && jobAcquisitionThread != null 
         && jobAcquisitionThread.isActive
       ) {
      jobAcquisitionThread.jobWasAdded();
    }
  }
  
  public void executeJobs(List<String> jobIds) {
    threadPoolExecutor.execute(new ExecuteJobsRunnable(commandExecutor, jobIds));
  }

  // getters and setters ////////////////////////////////////////////////////// 

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
  
  public int getWaitTimeInMillis() {
    return waitTimeInMillis;
  }
  
  public void setWaitTimeInMillis(int waitTimeInMillis) {
    this.waitTimeInMillis = waitTimeInMillis;
  }
  
  public int getLockTimeInMillis() {
    return lockTimeInMillis;
  }
  
  public void setLockTimeInMillis(int lockTimeInMillis) {
    this.lockTimeInMillis = lockTimeInMillis;
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
  
  public JobAcquisitionThread getJobAcquisitionThread() {
    return jobAcquisitionThread;
  }
  
  public void setJobAcquisitionThread(JobAcquisitionThread jobAcquisitionThread) {
    this.jobAcquisitionThread = jobAcquisitionThread;
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
  
  public boolean isActive() {
    return isActive;
  }
  
  public int getMaxJobsPerAcquisition() {
    return maxJobsPerAcquisition;
  }
  
  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition) {
    this.maxJobsPerAcquisition = maxJobsPerAcquisition;
  }
  
  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

}
