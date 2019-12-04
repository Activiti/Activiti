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
package org.activiti.engine.impl.bpmn.listener;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionPropagation;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;

/**
 * A {@link TransactionListener} that invokes an {@link ExecutionListener}.
 * 

 */
public class ExecuteTaskListenerTransactionListener implements TransactionListener {

  protected TransactionDependentTaskListener listener;
  protected TransactionDependentTaskListenerExecutionScope scope;
  
  public ExecuteTaskListenerTransactionListener(TransactionDependentTaskListener listener, 
      TransactionDependentTaskListenerExecutionScope scope) {
    this.listener = listener;
    this.scope = scope;
  }
  
  @Override
  public void execute(CommandContext commandContext) {
    CommandExecutor commandExecutor = commandContext.getProcessEngineConfiguration().getCommandExecutor();
    CommandConfig commandConfig = new CommandConfig(false, TransactionPropagation.REQUIRES_NEW);
    commandExecutor.execute(commandConfig, new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        listener.notify(scope.getProcessInstanceId(), scope.getExecutionId(), scope.getTask(), 
            scope.getExecutionVariables(), scope.getCustomPropertiesMap());
        return null;
      }
    });
  }

}
