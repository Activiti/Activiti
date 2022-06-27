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

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.ExecuteAsyncJobCmd;
import org.activiti.engine.impl.cmd.LockExclusiveJobCmd;
import org.activiti.engine.impl.cmd.UnlockExclusiveJobCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class ExecuteAsyncRunnable implements Runnable {

  private static Logger log = LoggerFactory.getLogger(ExecuteAsyncRunnable.class);

  protected String jobId;
  protected Job job;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public ExecuteAsyncRunnable(String jobId, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.jobId = jobId;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public ExecuteAsyncRunnable(Job job, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.job = job;
    this.jobId = job.getId();
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public void run() {

    if (job == null) {
      job = processEngineConfiguration.getCommandExecutor().execute(new Command<JobEntity>() {
        @Override
        public JobEntity execute(CommandContext commandContext) {
          return commandContext.getJobEntityManager().findById(jobId);
        }
      });
    }
    runInternal();
  }

  protected void runInternal(){
      boolean lockNotNeededOrSuccess = lockJobIfNeeded();

      if (lockNotNeededOrSuccess) {
          executeJob();
          unlockJobIfNeeded();
      }
  }

  protected void executeJob() {
    try {
      processEngineConfiguration.getCommandExecutor().execute(new ExecuteAsyncJobCmd(jobId));

    } catch (final ActivitiOptimisticLockingException e) {

      handleFailedJob(e);

      if (log.isDebugEnabled()) {
        log.debug("Optimistic locking exception during job execution. If you have multiple async executors running against the same database, "
            + "this exception means that this thread tried to acquire an exclusive job, which already was changed by another async executor thread."
            + "This is expected behavior in a clustered environment. " + "You can ignore this message if you indeed have multiple job executor threads running against the same database. "
            + "Exception message: {}", e.getMessage());
      }

    } catch (Throwable exception) {
      handleFailedJob(exception);

      // Finally, Throw the exception to indicate the ExecuteAsyncJobCmd failed
      String message = "Job " + jobId + " failed";
      log.error(message, exception);
    }
  }

  protected void unlockJobIfNeeded() {
    try {
      if (job.isExclusive()) {
        processEngineConfiguration.getCommandExecutor().execute(new UnlockExclusiveJobCmd(job));
      }

    } catch (ActivitiOptimisticLockingException optimisticLockingException) {
      if (log.isDebugEnabled()) {
        log.debug("Optimistic locking exception while unlocking the job. If you have multiple async executors running against the same database, "
            + "this exception means that this thread tried to acquire an exclusive job, which already was changed by another async executor thread."
            + "This is expected behavior in a clustered environment. " + "You can ignore this message if you indeed have multiple job executor acquisition threads running against the same database. "
            + "Exception message: {}", optimisticLockingException.getMessage());
      }

    } catch (Throwable t) {
      log.error("Error while unlocking exclusive job " + job.getId(), t);
    }
  }

  /**
   * Returns true if lock succeeded, or no lock was needed.
   * Returns false if locking was unsuccessfull.
   */
  protected boolean lockJobIfNeeded() {
    try {
      if (job.isExclusive()) {
        processEngineConfiguration.getCommandExecutor().execute(new LockExclusiveJobCmd(job));
      }

    } catch (Throwable lockException) {
      if (log.isDebugEnabled()) {
        log.debug("Could not lock exclusive job. Unlocking job so it can be acquired again. Catched exception: " + lockException.getMessage());
      }

      // Release the job again so it can be acquired later or by another node
      unacquireJob();

      return false;
    }

    return true;
  }

  protected void unacquireJob() {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      commandContext.getJobManager().unacquire(job);
    } else {
      processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          commandContext.getJobManager().unacquire(job);
          return null;
        }
      });
    }
  }

  protected void handleFailedJob(final Throwable exception) {
    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {

        CommandConfig commandConfig = processEngineConfiguration.getCommandExecutor().getDefaultConfig().transactionRequiresNew();
        FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
        Command<Object> cmd = failedJobCommandFactory.getCommand(job.getId(), exception);

        log.trace("Using FailedJobCommandFactory '" + failedJobCommandFactory.getClass() + "' and command of type '" + cmd.getClass() + "'");
        processEngineConfiguration.getCommandExecutor().execute(commandConfig, cmd);

        // Dispatch an event, indicating job execution failed in a
        // try-catch block, to prevent the original exception to be swallowed
        if (commandContext.getEventDispatcher().isEnabled()) {
          try {
            commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityExceptionEvent(ActivitiEventType.JOB_EXECUTION_FAILURE, job, exception));
          } catch (Throwable ignore) {
            log.warn("Exception occurred while dispatching job failure event, ignoring.", ignore);
          }
        }

        return null;
      }

    });
  }

}
