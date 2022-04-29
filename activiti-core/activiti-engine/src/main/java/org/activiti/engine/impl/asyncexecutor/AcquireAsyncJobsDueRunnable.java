/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *

 */
public class AcquireAsyncJobsDueRunnable implements Runnable {

  private static Logger log = LoggerFactory.getLogger(AcquireAsyncJobsDueRunnable.class);

  protected final AsyncExecutor asyncExecutor;

  protected volatile boolean isInterrupted;
  protected final Object MONITOR = new Object();
  protected final AtomicBoolean isWaiting = new AtomicBoolean(false);

  protected long millisToWait;

  public AcquireAsyncJobsDueRunnable(AsyncExecutor asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  public synchronized void run() {
    log.info("{} starting to acquire async jobs due");
    Thread.currentThread().setName("activiti-acquire-async-jobs");

    final CommandExecutor commandExecutor = asyncExecutor.getProcessEngineConfiguration().getCommandExecutor();

    while (!isInterrupted) {

      try {
        AcquiredJobEntities acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(asyncExecutor));

        boolean allJobsSuccessfullyOffered = true;
        for (JobEntity job : acquiredJobs.getJobs()) {
          boolean jobSuccessFullyOffered = asyncExecutor.executeAsyncJob(job);
          if (!jobSuccessFullyOffered) {
            allJobsSuccessfullyOffered = false;
          }
        }

        // If all jobs are executed, we check if we got back the amount we expected
        // If not, we will wait, as to not query the database needlessly.
        // Otherwise, we set the wait time to 0, as to query again immediately.
        millisToWait = asyncExecutor.getDefaultAsyncJobAcquireWaitTimeInMillis();
        int jobsAcquired = acquiredJobs.size();
        if (jobsAcquired >= asyncExecutor.getMaxAsyncJobsDuePerAcquisition()) {
          millisToWait = 0;
        }

        // If the queue was full, we wait too (even if we got enough jobs back), as not overload the queue
        if (millisToWait == 0 && !allJobsSuccessfullyOffered) {
          millisToWait = asyncExecutor.getDefaultQueueSizeFullWaitTimeInMillis();
        }

      } catch (ActivitiOptimisticLockingException optimisticLockingException) {
        if (log.isDebugEnabled()) {
          log.debug("Optimistic locking exception during async job acquisition. If you have multiple async executors running against the same database, "
              + "this exception means that this thread tried to acquire a due async job, which already was acquired by another async executor acquisition thread."
              + "This is expected behavior in a clustered environment. "
              + "You can ignore this message if you indeed have multiple async executor acquisition threads running against the same database. " + "Exception message: {}",
              optimisticLockingException.getMessage());
        }
      } catch (Throwable e) {
        log.error("exception during async job acquisition: {}", e.getMessage(), e);
        millisToWait = asyncExecutor.getDefaultAsyncJobAcquireWaitTimeInMillis();
      }

      if (millisToWait > 0) {
        try {
          if (log.isDebugEnabled()) {
            log.debug("async job acquisition thread sleeping for {} millis", millisToWait);
          }
          synchronized (MONITOR) {
            if (!isInterrupted) {
              isWaiting.set(true);
              MONITOR.wait(millisToWait);
            }
          }

          if (log.isDebugEnabled()) {
            log.debug("async job acquisition thread woke up");
          }
        } catch (InterruptedException e) {
          if (log.isDebugEnabled()) {
            log.debug("async job acquisition wait interrupted");
          }
        } finally {
          isWaiting.set(false);
        }
      }
    }

    log.info("{} stopped async job due acquisition");
  }

  public void stop() {
    synchronized (MONITOR) {
      isInterrupted = true;
      if (isWaiting.compareAndSet(true, false)) {
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
