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

import java.util.Map;

/**
 * Class handling invocations of {@link org.activiti.engine.delegate.TransactionDependentExecutionListener}
 * 
 * @author Yvo Swillens
 */
public class TransactionDependentExecutionListenerInvocation extends DelegateInvocation {

  protected final TransactionDependentExecutionListener executionListenerInstance;
  protected final FlowElement flowElement;
  protected final Map<String, Object> variables;

  public TransactionDependentExecutionListenerInvocation(TransactionDependentExecutionListener executionListenerInstance, FlowElement flowElement, Map<String, Object> variables) {
    this.executionListenerInstance = executionListenerInstance;
    this.flowElement = flowElement;
    this.variables = variables;
  }

  protected void invoke() {
    executionListenerInstance.notify(flowElement, variables);
  }

  public Object getTarget() {
    return executionListenerInstance;
  }

}
