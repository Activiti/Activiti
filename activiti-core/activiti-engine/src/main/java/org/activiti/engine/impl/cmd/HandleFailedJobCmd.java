/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class HandleFailedJobCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(HandleFailedJobCmd.class);

  protected String jobId;
  protected Throwable exception;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public HandleFailedJobCmd(
      String jobId,
      ProcessEngineConfigurationImpl processEngineConfiguration,
      Throwable exception) {
    this.jobId = jobId;
    this.processEngineConfiguration = processEngineConfiguration;
    this.exception = exception;
  }

  public Object execute(CommandContext commandContext) {
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId and job is null");
    }

    Job job = commandContext.getJobEntityManager().findById(jobId);

    if (job == null) {
      throw new JobNotFoundException(jobId);
    }

    if (log.isDebugEnabled()) {
      log.debug("Executing job {}", job.getId());
    }

    executeInternal(commandContext,job);
    return null;
  }

  protected void executeInternal(CommandContext commandContext, Job job) {
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
  }
}
