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
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *

 */
public class AcquireTimerJobsRunnable implements Runnable {

  private static Logger log = LoggerFactory.getLogger(AcquireTimerJobsRunnable.class);

  protected final AsyncExecutor asyncExecutor;
  protected final JobManager jobManager;

  protected volatile boolean isInterrupted;
  protected final Object MONITOR = new Object();
  protected final AtomicBoolean isWaiting = new AtomicBoolean(false);

  protected long millisToWait;

  public AcquireTimerJobsRunnable(AsyncExecutor asyncExecutor, JobManager jobManager) {
    this.asyncExecutor = asyncExecutor;
    this.jobManager = jobManager;
  }

  public synchronized void run() {
    log.info("{} starting to acquire async jobs due");
    Thread.currentThread().setName("activiti-acquire-timer-jobs");

    final CommandExecutor commandExecutor = asyncExecutor.getProcessEngineConfiguration().getCommandExecutor();

    while (!isInterrupted) {

      try {
        final AcquiredTimerJobEntities acquiredJobs = commandExecutor.execute(new AcquireTimerJobsCmd(asyncExecutor));

        commandExecutor.execute(new Command<Void>() {

          @Override
          public Void execute(CommandContext commandContext) {
            for (TimerJobEntity job : acquiredJobs.getJobs()) {
              jobManager.moveTimerJobToExecutableJob(job);
            }
            return null;
          }
        });

        // if all jobs were executed
        millisToWait = asyncExecutor.getDefaultTimerJobAcquireWaitTimeInMillis();
        int jobsAcquired = acquiredJobs.size();
        if (jobsAcquired >= asyncExecutor.getMaxTimerJobsPerAcquisition()) {
          millisToWait = 0;
        }

      } catch (ActivitiOptimisticLockingException optimisticLockingException) {
        if (log.isDebugEnabled()) {
          log.debug("Optimistic locking exception during timer job acquisition. If you have multiple timer executors running against the same database, "
              + "this exception means that this thread tried to acquire a timer job, which already was acquired by another timer executor acquisition thread."
              + "This is expected behavior in a clustered environment. "
              + "You can ignore this message if you indeed have multiple timer executor acquisition threads running against the same database. " + "Exception message: {}",
              optimisticLockingException.getMessage());
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
            if (!isInterrupted) {
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
