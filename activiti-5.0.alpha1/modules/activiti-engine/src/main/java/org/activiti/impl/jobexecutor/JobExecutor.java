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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.activiti.impl.CmdExecutor;
import org.activiti.impl.ProcessEngineImpl;

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

  private ProcessEngineImpl processEngine;
  
  /**
   * Holds the pool of threads we have available
   *  to do work for us.
   */
  private ThreadPoolExecutor threadPool;
  private BlockingQueue<Runnable> threadPoolQueue;
  private PendingJobsFetcher pendingJobsFetcher;
  private HistoricJobsList historicJobsList;
  
  /**
   * How many background threads should we run?
   * Default of 3.
   */
  private int numberOfWorkerThreads = 3;
  /**
   * How many jobs should we should buffer in
   *  memory whilst waiting for a spare thread
   *  in the pool.
   * On one machine - the higher the number,
   *  the better the throughput.
   * On a cluster - the higher the number, the
   *  better the throughput, but at the 
   *  expense of a poorer ability to 
   *  balance load within the cluster.
   * Cannot be changed whilst the 
   *  executor is running!
   * Default is 3, the same size as the
   *  default number of background threads.
   */
  private int inMemoryQueueSize = 3;
  /**
   * How many jobs should we track in the
   *  historically executed jobs list?
   * This list can be used for debugging,
   *  performance checking and similar.
   * Jobs will roll out of this list
   *  when newer ones arrive.
   * Cannot be changed whilst the 
   *  executor is running!
   */
  private int historySize = 200;
  /**
   * How long (in milliseconds) should we 
   *  normally wait between checks of the queue?
   * Default of 5 seconds.
   */
  private int defaultPollInterval = 5 * 1000;
  /**
   * How long (in milliseconds) should we 
   *  wait when all the workers are busy,
   *  before we try again to give them work
   *  from the queue?
   * Default is 0.1 seconds 
   */
  private int workersBusyPollInterval = 100;
  /**
   * How long should a Thread be able to be working
   *  on a Job without updates, before we decide that
   *  it has probably died?
   * Default of 30 minutes
   */
  private int maxLockDuration = 30 * 60 * 1000;
  
  private volatile boolean active = false;
  
  /**
   * Fires up the various background threads.
   */
  public synchronized void start() {
    if(processEngine == null) {
      throw new IllegalStateException("Process Engine not given");
    }
    
    if(active) {
      // Already started, nothing to do
      log.info("Ignoring duplicate JobExecutor start request");
      return;
    } else {
      active = true;
      
      // Create the historic jobs list
      historicJobsList = new HistoricJobsList(historySize);
      
      // Create our Threading pool
      log.info("JobExecutor is starting the Thread Pool");
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(inMemoryQueueSize);
      threadPool = new ThreadPoolExecutor(
          numberOfWorkerThreads,
          numberOfWorkerThreads,
          0L,
          TimeUnit.MILLISECONDS,
          threadPoolQueue,
          new RetryingThreadExecutionHandler(this)
      );
      
      // Create our pending jobs fetcher
      log.info("JobExecutor is starting the Pending Jobs Fetcher");
      pendingJobsFetcher = new PendingJobsFetcher(this);
      pendingJobsFetcher.start();
      
      // All good to go
      log.info("JobExecutor is active");
    }
  }
  
  /**
   * Triggers a graceful shutdown, where work in-progress
   *  continues, but no new work is accepted.
   * @param waitForCompletion Should the call block until completed?
   */
  public void shutdownGraceful(boolean waitForCompletion) {
    if(!active) {
      log.info("Ignoring request to shut down non-active JobExecutor");
      return;
    }
    
    active = false;
    log.info("Gracefully shutting down the JobExecutor");
    
    // Close the pending jobs task
    pendingJobsFetcher.shutdown();
    
    // Ask the thread pool to finish and exit
    threadPool.shutdown();
    
    if(waitForCompletion) {
      try {
        threadPool.awaitTermination(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {}
      pendingJobsFetcher.awaitShutdownComplete();
      
      log.info("JobExecutor has shutdown");
    } else {
      log.info("JobExecutor has requested all running jobs cease shortly");
    }
  }
  
  /**
   * Triggers an immediate shutdown. Work in progress will
   *  be cancelled and re-queued, and no new work is
   *  accepted.
   * @param waitForCompletion Should the call block until completed?
   */
  public void shutdownImmediate(boolean waitForCompletion) {
    if(!active) {
      log.info("Ignoring request to shut down non-active JobExecutor");
      return;
    }
    
    active = false;
    log.info("Forcefully shutting down the JobExecutor");
    
    // Close the pending jobs task
    pendingJobsFetcher.shutdown();
    
    // Close the thread pool, giving back what was in progress
    List<Runnable> inProgress = threadPool.shutdownNow();
    for(Runnable job : inProgress) {
      if(job instanceof BackgroundJob) {
        BackgroundJob task = (BackgroundJob)job;
        
        // TODO - force cancel on the job/transaction
      }
      
      // Mark us as being unable to complete this
      giveBack(job);
    }
    
    log.info("JobExecutor has shut down");
  }
  
  /**
   * Called when a job cannot be run due to capacity or
   *  shutdown, to put it back onto the queue for 
   *  processing later / by a different Executor.
   */
  protected void giveBack(Runnable job) {
    if(job instanceof BackgroundJob) {
      BackgroundJob task = (BackgroundJob)job;
      
      // TODO - update the database to say that we're
      //  not handling this job after all
    } else {
      log.warning("Found unexpected item on run queue when trying to give back - " + job);
    }
  }
  
  /**
   * Executes the given job.
   */
  protected void execute(BackgroundJob job) {
    if(active) {
      threadPool.execute(job);
    }
  }
  
  /**
   * Adds a completed job to the historic
   *  jobs list.
   */
  protected void recordExecutionComplete(HistoricJob job) {
    historicJobsList.record(job);
  }
  
  /**
   * Used to hint that new work exists on the
   *  queue, and that the {@link PendingJobsFetcher}
   *  should probably re-check for jobs.
   */
  public void jobWasAdded() {
    if(active && pendingJobsFetcher != null &&
        pendingJobsFetcher.isActive) {
      pendingJobsFetcher.jobWasAdded();
    }
  }
  
  
  /**
   * Returns roughly how many threads are available
   *  (not currently working).
   * Used when deciding how many Jobs to retrieve from
   *  the queue for execution.
   */
  public int getApproxNumberOfAvailableThreads() {
    return threadPool.getMaximumPoolSize() -
      threadPool.getActiveCount();
  }
  /**
   * Returns roughly how many Jobs can be fetched
   *  and pushed onto the queue without blocking.
   * Sum of the spare space on the queue, and the
   *  spare threads.
   * Used when deciding how many Jobs to retrieve from
   *  the queue for execution.
   */
  public int getAvailableJobSlots() {
    return getApproxNumberOfAvailableThreads() +
      threadPoolQueue.remainingCapacity();
  }

  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public CmdExecutor getCmdExecutor() {
    return processEngine.getCmdExecutor();
  }
  
  public HistoricJobsList getHistoricJobsList() {
    return historicJobsList;
  }

  public int getNumberOfWorkerThreads() {
    return numberOfWorkerThreads;
  }
  public void setNumberOfWorkerThreads(int numberOfWorkerThreads) {
    this.numberOfWorkerThreads = numberOfWorkerThreads;
    
    // Have this reflected in the pool if running
    if(threadPool != null) {
      // Update the Thread Pool
      // If size is decreasing, the thread pool will reduce
      //  when jobs finish
      // If increasing, the thread pool will increase when
      //  new jobs are added
      threadPool.setMaximumPoolSize(numberOfWorkerThreads);
      threadPool.setCorePoolSize(numberOfWorkerThreads);
    }
  }

  public int getInMemoryQueueSize() {
    return inMemoryQueueSize;
  }
  public void setInMemoryQueueSize(int inMemoryQueueSize) {
    if(active) {
      throw new IllegalStateException("The In-Memory Queue Size may not be changed on an active JobExecutor!");
    }
    this.inMemoryQueueSize = inMemoryQueueSize;
  }

  public int getHistorySize() {
    return historySize;
  }
  public void setHistorySize(int historySize) {
    if(active) {
      throw new IllegalStateException("The History Size may not be changed on an active JobExecutor!");
    }
    this.historySize = historySize;
  }

  public int getDefaultPollInterval() {
    return defaultPollInterval;
  }
  public void setDefaultPollInterval(int defaultPollInterval) {
    this.defaultPollInterval = defaultPollInterval;
  }

  public int getWorkersBusyPollInterval() {
    return workersBusyPollInterval;
  }
  public void setWorkersBusyPollInterval(int workersBusyPollInterval) {
    this.workersBusyPollInterval = workersBusyPollInterval;
  }

  public int getMaxLockDuration() {
    return maxLockDuration;
  }
  public void setMaxLockDuration(int maxLockDuration) {
    this.maxLockDuration = maxLockDuration;
  }

  public boolean isActive() {
    return active;
  }
}
