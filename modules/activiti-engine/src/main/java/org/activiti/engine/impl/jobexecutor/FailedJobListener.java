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

import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Frederik Heremans
 */
public class FailedJobListener implements TransactionListener {
  private static final Logger log = LoggerFactory.getLogger(FailedJobListener.class);

  protected CommandExecutor commandExecutor;
  protected String jobId;
  protected Throwable exception;

  public FailedJobListener(CommandExecutor commandExecutor, String jobId, Throwable exception) {
    this.commandExecutor = commandExecutor;
    this.jobId = jobId;
    this.exception = exception;
  }
  
  public void execute(CommandContext commandContext) {
    try {
      CommandConfig commandConfig = commandExecutor.getDefaultConfig().transactionRequiresNew();
      Command<Object> failedJobCommand = commandContext.getFailedJobCommandFactory().getCommand(jobId, exception);
      commandExecutor.execute(commandConfig, failedJobCommand);
    } catch (Throwable t) {
      // When there is an error while handling failed jobs (decrementing retries) this
      // should be logged because it's a serious issue
      log.warn("Error while executing command when job is failed for job: '" + jobId + "'.", t);
      
      // Re-throw the exception
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      throw new RuntimeException(t);
    }
  }

}
