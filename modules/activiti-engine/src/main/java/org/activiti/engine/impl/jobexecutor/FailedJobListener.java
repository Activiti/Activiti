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
 * @author Saeid Mirzaei
 */
public class FailedJobListener implements TransactionListener {
  private static final Logger log = LoggerFactory.getLogger(FailedJobListener.class);

  protected CommandExecutor commandExecutor;
  protected String jobId;
  protected Throwable exception;
  
  public FailedJobListener(CommandExecutor commandExecutor, String jobId) {
    this.commandExecutor = commandExecutor;
    this.jobId = jobId;
  }
  
  public void execute(CommandContext commandContext) {
    CommandConfig commandConfig = commandExecutor.getDefaultConfig().transactionRequiresNew();
	  FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
	  Command<Object> cmd = failedJobCommandFactory.getCommand(jobId, exception);

	  log.trace("Using FailedJobCommandFactory '" + failedJobCommandFactory.getClass() + "' and command of type '" + cmd.getClass() + "'");
	  commandExecutor.execute(commandConfig, cmd);
  }
  
  public void setException(Throwable exception) {
    this.exception = exception;
  }

}
