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

import java.util.concurrent.atomic.AtomicBoolean;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tijs Rademakers
 */
public class AcquireTimerJobsRunnable implements Runnable {

  private static Logger log = LoggerFactory.getLogger(AcquireTimerJobsRunnable.class);

  protected final AsyncExecutor asyncExecutor;

  protected volatile boolean isInterrupted = false;
  protected final Object MONITOR = new Object();
  protected final AtomicBoolean isWaiting = new AtomicBoolean(false);
  
  protected long millisToWait = 0;

  public AcquireTimerJobsRunnable(AsyncExecutor asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  public synchronized void run() {
    log.info("starting to acquire async jobs due");

    final CommandExecutor commandExecutor = asyncExecutor.getCommandExecutor();

    while (!isInterrupted) {
      
      try {
        AcquiredJobEntities acquiredJobs = commandExecutor.execute(new AcquireTimerJobsCmd(
            asyncExecutor.getLockOwner(), asyncExecutor.getTimerLockTimeInMillis(), 
            asyncExecutor.getMaxTimerJobsPerAcquisition()));
        
        boolean allJobsSuccessfullyOffered = true; 
        for (JobEntity job : acquiredJobs.getJobs()) {
          boolean jobSuccessFullyOffered = asyncExecutor.executeAsyncJob(job);
          if (!jobSuccessFullyOffered) {
            allJobsSuccessfullyOffered = false;
          }
        }
        
        // if all jobs were executed
        millisToWait = asyncExecutor.getDefaultTimerJobAcquireWaitTimeInMillis();
        int jobsAcquired = acquiredJobs.size();
        if (jobsAcquired >= asyncExecutor.getMaxTimerJobsPerAcquisition()) {
          millisToWait = 0; 
        }
        
        // If the queue was full, we wait too (even if we got enough jobs back), as not overload the queue
        if (millisToWait == 0 && !allJobsSuccessfullyOffered) {
          millisToWait = asyncExecutor.getDefaultQueueSizeFullWaitTimeInMillis();
        }

      } catch (ActivitiOptimisticLockingException optimisticLockingException) { 
        if (log.isDebugEnabled()) {
          log.debug("Optimistic locking exception during timer job acquisition. If you have multiple timer executors running against the same database, " +
              "this exception means that this thread tried to acquire a timer job, which already was acquired by another timer executor acquisition thread." +
              "This is expected behavior in a clustered environment. " +
              "You can ignore this message if you indeed have multiple timer executor acquisition threads running against the same database. " +
              "Exception message: {}", optimisticLockingException.getMessage());
        }
      } catch (Throwable e) {
        log.error("exception during timer job acquisition: {}", e.getMessage(), e);          
        millisToWait = asyncExecutor.getDefaultTimerJobAcquireWaitTimeInMillis();
      }

      if (millisToWait > 0) {
        try {
          if (log.isDebugEnabled()) {
            log.debug("timer job acquisition thread sleeping for {} millis", millisToWait);
          }
          synchronized (MONITOR) {
            if(!isInterrupted) {
              isWaiting.set(true);
              MONITOR.wait(millisToWait);
            }
          }
          
          if (log.isDebugEnabled()) {
            log.debug("timer job acquisition thread woke up");
          }
        } catch (InterruptedException e) {
          if (log.isDebugEnabled()) {
            log.debug("timer job acquisition wait interrupted");
          }
        } finally {
          isWaiting.set(false);
        }
      }
    }
    
    log.info("stopped async job due acquisition");
  }

  public void stop() {
    synchronized (MONITOR) {
      isInterrupted = true; 
      if(isWaiting.compareAndSet(true, false)) { 
          MONITOR.notifyAll();
        }
      }
  }

  public long getMillisToWait() {
    return millisToWait;
  }
  
  public void setMillisToWait(long millisToWait) {
    this.millisToWait = millisToWait;
  }
}
