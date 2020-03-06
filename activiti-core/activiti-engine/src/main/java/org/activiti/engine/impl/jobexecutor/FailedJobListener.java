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

package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**



 */
public class FailedJobListener implements CommandContextCloseListener {
  
  private static final Logger log = LoggerFactory.getLogger(FailedJobListener.class);

  protected CommandExecutor commandExecutor;
  protected Job job;

  public FailedJobListener(CommandExecutor commandExecutor, Job job) {
    this.commandExecutor = commandExecutor;
    this.job = job;
  }

  @Override
  public void closing(CommandContext commandContext) {
  }

  @Override
  public void afterSessionsFlush(CommandContext commandContext) {
  }

  @Override
  public void closed(CommandContext context) {
    if (context.getEventDispatcher().isEnabled()) {
      context.getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_EXECUTION_SUCCESS, job));
    }
  }

  @Override
  public void closeFailure(CommandContext commandContext) {
    if (commandContext.getEventDispatcher().isEnabled()) {
      commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityExceptionEvent(
        ActivitiEventType.JOB_EXECUTION_FAILURE, job, commandContext.getException()));
    }
    
    CommandConfig commandConfig = commandExecutor.getDefaultConfig().transactionRequiresNew();
    FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
    Command<Object> cmd = failedJobCommandFactory.getCommand(job.getId(), commandContext.getException());

    log.trace("Using FailedJobCommandFactory '" + failedJobCommandFactory.getClass() + "' and command of type '" + cmd.getClass() + "'");
    commandExecutor.execute(commandConfig, cmd);
  }

}
