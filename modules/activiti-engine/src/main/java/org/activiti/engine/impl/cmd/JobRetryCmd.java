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
package org.activiti.engine.impl.cmd;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.MessageAddedNotification;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.DefaultClockImpl;

/**
 * @author Saeid Mirzaei
 */

public class JobRetryCmd implements Command<Object> {

  private static final Logger log = Logger.getLogger(JobRetryCmd.class.getName());

  protected String jobId;
  protected Throwable exception;

  public JobRetryCmd(String jobId, Throwable exception) {
    this.jobId = jobId;
    this.exception = exception;
  }

  public Object execute(CommandContext commandContext) {
    JobEntity job = Context
		      .getCommandContext()
		      .getJobEntityManager()
		      .findJobById(jobId);
    
    ActivityImpl activity = getCurrentActivity(commandContext, job);

    if (activity == null) {
      log.log(Level.SEVERE, "Failure while executing " + JobRetryCmd.class.getName() + " for job id '" + jobId + "'. Falling back to standard job retry strategy.");
      executeStandardStrategy(commandContext);
    } else {
      try {
        executeCustomStrategy(commandContext, job, activity);
      } catch (Exception e) {
        log.log(Level.SEVERE, "Failure while executing " + JobRetryCmd.class.getName() + " for job id '" + jobId + "'. Falling back to standard job retry strategy.", e);
        executeStandardStrategy(commandContext);
      }
    }

    return null;
  }

  private void executeCustomStrategy(CommandContext commandContext, JobEntity job, ActivityImpl activity) throws Exception {
    String failedJobRetryTimeCycle = activity.getFailedJobRetryTimeCycleValue();

    if(failedJobRetryTimeCycle == null) {
      executeStandardStrategy(commandContext);

    } else {
      DurationHelper durationHelper = new DurationHelper(failedJobRetryTimeCycle, Context.getProcessEngineConfiguration().getClock());
      job.setLockExpirationTime(durationHelper.getDateAfter());

      // check if this is jobs' first execution (recognize this because no exception is set. Only the first execution can be without exception - because if no exception occurred the job would have been completed)
     // job.getExceptionByteArrayId() == null &&
      if (job.getExceptionMessage()==null) {
          log.fine("Applying JobRetryStrategy '" + failedJobRetryTimeCycle+ "' the first time for job " + job.getId() + " with "+durationHelper.getTimes()+" retries");
        // then change default retries to the ones configured
        job.setRetries(durationHelper.getTimes());
      }
      else {
      	log.fine("Decrementing retries of JobRetryStrategy '" + failedJobRetryTimeCycle+ "' for job " + job.getId());
      }

      if (exception != null) {
        job.setExceptionMessage(exception.getMessage());
        job.setExceptionStacktrace(getExceptionStacktrace());
      }

      job.setRetries(job.getRetries() - 1);

      JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
      MessageAddedNotification messageAddedNotification = new MessageAddedNotification(jobExecutor);
      TransactionContext transactionContext = commandContext.getTransactionContext();
      transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
    }
  }

  private ActivityImpl getCurrentActivity(CommandContext commandContext, JobEntity job) {
    String type = job.getJobHandlerType();
    ActivityImpl activity = null;

    if (TimerExecuteNestedActivityJobHandler.TYPE.equals(type) ||
        TimerCatchIntermediateEventJobHandler.TYPE.equals(type)) {
      ExecutionEntity execution = fetchExecutionEntity(job.getExecutionId());
      if (execution != null) {
        activity = execution.getProcessDefinition().findActivity(job.getJobHandlerConfiguration());
      }
    } else if (TimerStartEventJobHandler.TYPE.equals(type)) {
    	DeploymentManager deploymentManager = Context.getProcessEngineConfiguration().getDeploymentManager();
      ProcessDefinitionEntity processDefinition =  deploymentManager.findDeployedLatestProcessDefinitionByKey(job.getJobHandlerConfiguration());
      if (processDefinition != null) {
        activity = processDefinition.getInitial();
      }
    } else if (AsyncContinuationJobHandler.TYPE.equals(type)) {
      ExecutionEntity execution = fetchExecutionEntity(job.getExecutionId());
      if (execution != null) {
        activity = execution.getActivity();
      }
    } else {
      // nop, because activity type is not supported
    }

    return activity;
  }

  private String getExceptionStacktrace() {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private ExecutionEntity fetchExecutionEntity(String executionId) {
    return Context.getCommandContext()
                  .getExecutionEntityManager()
                  .findExecutionById(executionId);
  }

  private void executeStandardStrategy(CommandContext commandContext) {
    DecrementJobRetriesCmd decrementCmd = new DecrementJobRetriesCmd(jobId, exception);
    decrementCmd.execute(commandContext);
  }


}
