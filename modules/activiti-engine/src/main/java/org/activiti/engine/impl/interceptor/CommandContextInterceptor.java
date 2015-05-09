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

package org.activiti.engine.impl.interceptor;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.jobexecutor.FailedJobListener;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 */
public class CommandContextInterceptor extends AbstractCommandInterceptor {
  private static final Logger log = LoggerFactory.getLogger(CommandContextInterceptor.class);

  protected CommandContextFactory commandContextFactory;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public CommandContextInterceptor() {
  }

  public CommandContextInterceptor(CommandContextFactory commandContextFactory, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.commandContextFactory = commandContextFactory;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public <T> T execute(CommandConfig config, Command<T> command) {
    CommandContext context = Context.getCommandContext();

    boolean contextReused = false;
    // We need to check the exception, because the transaction can be in a
    // rollback state, and some other command is being fired to compensate (eg. decrementing job retries)
    if (!config.isContextReusePossible() || context == null || context.getException() != null) {
      context = commandContextFactory.createCommandContext(command);
    } else {
      log.debug("Valid context found. Reusing it for the current command '{}'", command.getClass().getCanonicalName());
      contextReused = true;
    }

    try {
      // Push on stack
      Context.setCommandContext(context);
      Context.setProcessEngineConfiguration(processEngineConfiguration);

      return next.execute(config, command);

    } catch (Exception e) {

      context.exception(e);
      
      if (context.isManualJobExecution() && !contextReused) {
        handleManualJobFailure(context, e);
      }

    } finally {
      try {
        if (!contextReused) {
          context.close();
        }
      } finally {
        
        if (context.isManualJobExecution() && !contextReused && context.getException() == null) { // !contextReused -> only needed on highest level
          handleManualJobSuccess(context);
        }
        
        // Pop from stack
        Context.removeCommandContext();
        Context.removeProcessEngineConfiguration();
      }
    }

    return null;
  }

  protected void handleManualJobSuccess(CommandContext context) {
    if (context.getEventDispatcher().isEnabled()) {
      context.getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_EXECUTION_SUCCESS, 
          Context.getJobExecutorContext().getCurrentJob()));
    }
  }

  protected void handleManualJobFailure(CommandContext context, Exception e) {
    FailedJobListener failedJobListener = null;
    
    // When transaction is rolled back, decrement retries
    JobEntity currentJob = Context.getJobExecutorContext().getCurrentJob();
    failedJobListener = new FailedJobListener(context.getProcessEngineConfiguration().getCommandExecutor(), currentJob.getId());
    failedJobListener.setException(e);
    context.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, failedJobListener);
    
    if (context.getEventDispatcher().isEnabled()) {
      try {
        context.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityExceptionEvent(
            ActivitiEventType.JOB_EXECUTION_FAILURE, currentJob, e));
      } catch(Throwable ignore) {
        log.warn("Exception occured while dispatching job failure event, ignoring.", ignore);
      }
    }
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineContext(ProcessEngineConfigurationImpl processEngineContext) {
    this.processEngineConfiguration = processEngineContext;
  }
}
