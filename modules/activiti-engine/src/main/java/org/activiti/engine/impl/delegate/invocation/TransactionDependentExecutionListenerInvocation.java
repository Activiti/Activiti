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
package org.activiti.engine.impl.delegate.invocation;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.impl.cfg.TransactionPropagation;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Map;

/**
 * Class handling invocations of {@link org.activiti.engine.delegate.TransactionDependentExecutionListener}
 * 
 * @author Yvo Swillens
 */
public class TransactionDependentExecutionListenerInvocation extends DelegateInvocation {

  protected final CommandContext commandContext;
  protected final TransactionDependentExecutionListener executionListenerInstance;
  protected final String processInstanceId;
  protected final String executionId;
  protected final FlowElement flowElement;
  protected final Map<String, Object> executionVariables;
  protected final Map<String, Object> customPropertiesMap;

  public TransactionDependentExecutionListenerInvocation(CommandContext commandContext, TransactionDependentExecutionListener executionListenerInstance, String processInstanceId, String executionId,
                                                         FlowElement flowElement, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    this.commandContext = commandContext;
    this.executionListenerInstance = executionListenerInstance;
    this.processInstanceId = processInstanceId;
    this.executionId = executionId;
    this.flowElement = flowElement;
    this.executionVariables = executionVariables;
    this.customPropertiesMap = customPropertiesMap;
  }

  protected void invoke() {
    CommandExecutor commandExecutor = commandContext.getProcessEngineConfiguration().getCommandExecutor();
    CommandConfig commandConfig = new CommandConfig(false, TransactionPropagation.REQUIRES_NEW);
    commandExecutor.execute(commandConfig, new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        executionListenerInstance.notify(processInstanceId, executionId, flowElement, executionVariables, customPropertiesMap);
        return null;
      }
    });
  }

  public Object getTarget() {
    return executionListenerInstance;
  }

}
