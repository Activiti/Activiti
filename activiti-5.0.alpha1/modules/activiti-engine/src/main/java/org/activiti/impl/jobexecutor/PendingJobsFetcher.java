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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.impl.cmd.FindPendingJobsCmd;

/**
 * Background thread responsible for retrieving the list
 *  of Jobs currently awaiting processing from the queue, 
 *  and passing them to the {@link JobExecutor} to be run.
 * There should only ever be one of these per 
 *  {@link JobExecutor}.
 * Note that in a clustered Environment, there can be multiple
 *  of these per queue, so we need locking/transactions to 
 *  ensure we don't fetch Jobs someone else already has, and
 *  we need to ensure we're not too greedy about picking up
 *  jobs.
 */
public class PendingJobsFetcher extends Thread {
  private static Logger log = Logger.getLogger(PendingJobsFetcher.class.getName());
  private Thread runningThread;

  private JobExecutor jobExecutor;
  protected Object semaphore = new Object();
  
  protected volatile boolean isActive = true;
  protected boolean checkForNewJobs;
  
  protected int expectedSleepTime;
  
  protected PendingJobsFetcher(JobExecutor parent) {
    this(parent, "PendingJobsFetcher");
  }
  protected PendingJobsFetcher(JobExecutor parent, String name) {
    super(name);
    this.jobExecutor = parent;
  }

  /**
   * The main processing loop:
   *  * sleep
   *  * fetch
   *  * queue
   */
  public void run() {
    log.info(getName() + " has begun processing");
    runningThread = Thread.currentThread();

    int currentIdlePeriod = jobExecutor.getDefaultPollInterval();
    
    while(isActive) {
      try {
        // How many jobs should we limit ourselves to fetching?
        int limit = jobExecutor.getAvailableJobSlots();
        log.fine("Checking for up to " + limit + " jobs in need of execution");
        
        // Fetch
        checkForNewJobs = false;
        Collection<Collection<Long>> jobIds = getJobs(limit);
        
        // Have them sent to be executed
        if(jobIds.size() > 0) {
          submitAquiredJobs(jobIds);
        }
        
        // Reset the idle interval as the calls worked
        currentIdlePeriod = jobExecutor.getDefaultPollInterval();
        
        // How long should we wait for?
        long waitDuration;
        if(jobIds.size() == limit) {
          // Fetched as many as we could, need to
          //  wait while they run
          waitDuration = jobExecutor.getWorkersBusyPollInterval();
        } else if(checkForNewJobs) {
          // New work came in whilst we were submitting
          //  jobs, so carry straight on
          waitDuration = 0;
        } else {
          // We fetched as much as we could do
          // Sleep for the minimum of the next bit of 
          //  work, or our usual sleep time
          waitDuration = Math.min(
              currentIdlePeriod, 
              getTimeUntilNextJob()
          );
        }

        // Sleep before our next check
        if(waitDuration > 0) {
          synchronized (semaphore) {
            if(!checkForNewJobs) {
              log.fine("Waiting up to " + waitDuration + " before looking for new work");
              semaphore.wait(waitDuration);
            }
          }
        }
      } catch(InterruptedException ie) {
        // Some new work has probably arrived
        log.fine("Woken up from sleeping, active is now " + isActive);
      } catch(Exception e) {
        // Log that we hit an issue
        log.log(Level.SEVERE, "Unable to check for new work, will retry in " + currentIdlePeriod, e);
        
        // Sleep a bit
        try {
          synchronized (semaphore) {
            semaphore.wait(currentIdlePeriod);
          }
        } catch(InterruptedException ie2) {}
        
        // Double the next error wait time, in case
        //  of major problems
        currentIdlePeriod *= 2;
      }
    }
    
    runningThread = null;
    log.info(getName() + " has shutdown");
  }
  
  /**
   * Wraps the supplied list of sets of Job Ids as 
   *  either {@link BackgroundJob} or
   *  {@link BackgroundJobCollection}, and passes them
   *  off to the {@link JobExecutor} to be run.
   */
  protected void submitAquiredJobs(Collection<Collection<Long>> jobIds) {
    log.info("Submitting jobs for execution - " + jobIds);
    for(Collection<Long> jobIdSet : jobIds) {
      // Is it a single job, or a dependent set that
      //  need to be run on the same Thread?
      if(jobIdSet.size() == 1) {
        long jobId = jobIdSet.iterator().next();
        submitAquiredJob(jobId);
      } else {
        submitAquiredJobCollection(jobIdSet);
      }
    }
    log.info("Sucessfully submitted " + jobIds.size() + " jobs for execution");
  }
  
  /**
   * Wraps the supplied Job Ids as a 
   * {@link BackgroundJob}, and passes it
   *  off to the {@link JobExecutor} to be run.
   */
  private void submitAquiredJob(long jobId) {
    BackgroundJob job = new BackgroundJob(
        jobId,
        jobExecutor.getCmdExecutor(),
        jobExecutor.getProcessEngine(),
        jobExecutor.getHistoricJobsList()
    );
    jobExecutor.execute(job);
    log.info("Sucessfully submitted job for execution - " + jobId);
  }
  
  /**
   * Wraps the supplied list of Job Ids as a 
   *  {@link BackgroundJobCollection}, and passes 
   *  them off to the {@link JobExecutor} to be run.
   */
  protected void submitAquiredJobCollection(Collection<Long> jobIds) {
    log.info("Submitting jobs for execution - " + jobIds);
    BackgroundJobCollection job = new BackgroundJobCollection(
        jobIds,
        jobExecutor.getCmdExecutor(),
        jobExecutor.getProcessEngine(),
        jobExecutor.getHistoricJobsList()
    );
    jobExecutor.execute(job);
    log.info("Sucessfully submitted jobs for execution - " + jobIds);
  }
  
  /**
   * Fetches from the queue the list of Job IDs that
   *  need to be executed at the moment.
   * The limit should be the maximum number of jobs
   *  that the {@link JobExecutor} can handle, to 
   *  avoid us being greedy and accepting jobs
   *  we can't process yet.
   * All jobs within any one list must be run in
   *  sequence on the same Thread, while jobs
   *  in different lists can be run in parallel.
   */
  protected Collection<Collection<Long>> getJobs(int limit) {
    FindPendingJobsCmd cmd = new FindPendingJobsCmd(limit);

    Collection<Collection<Long>> ids =
      jobExecutor.getCmdExecutor().execute(cmd, jobExecutor.getProcessEngine());

    return ids;
  }
  
  /**
   * Fetches from the queue the date that the next
   *  Job will become available at.
   * This date could be in the past, if there is
   *  work waiting on the queue that needs to
   *  be run.
   */
  protected Date getNextJobAt() {
    // TODO Fetch the real date from the database
    return null;
  }
  /**
   * How long until {@link #getNextJobAt()} ?
   */
  protected long getTimeUntilNextJob() {
    Date next = getNextJobAt();
    if(next == null) {
      return Long.MAX_VALUE;
    }
    long time = next.getTime() - System.currentTimeMillis();
    
    if(time < 0) {
      // Due already!
      return 0;
    } else {
      return time;
    }
  }
  
  
  /**
   * Triggers a shutdown
   */
  public void shutdown() {
    if(isActive) {
      log.info(getName() + " is shutting down");
      isActive = false;
      
      try {
        semaphore.notify();
      } catch(IllegalMonitorStateException e) {}
    }
  }
  protected void awaitShutdownComplete() {
    if(isActive) {
      throw new IllegalStateException("Cannot await the shutdown of an active fetcher!");
    }
    if(runningThread != null) {
      try {
        runningThread.join();
      } catch(NullPointerException npe) {
      } catch(InterruptedException ie) {}
    }
  }
  
  /**
   * Used to hint that new work exists on the
   *  queue, and that we should probably
   *  wake up if sleeping and re-check
   */
  protected void jobWasAdded() {
    synchronized (semaphore) {
      // Wake up the main thread, and have it 
      //  check the queue for the job
      checkForNewJobs = true;
      
      try {
        semaphore.notify();
      } catch(IllegalMonitorStateException e) {}
    }
  }
  
  public boolean isActive() {
    return isActive;
  }
  protected int getExpectedSleepTime() {
    return expectedSleepTime;
  }
}
