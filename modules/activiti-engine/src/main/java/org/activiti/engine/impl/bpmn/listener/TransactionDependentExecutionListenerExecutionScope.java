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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;

import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class TransactionDependentExecutionListenerExecutionScope {

  protected final TransactionDependentExecutionListener executionListener;
  protected final String processInstanceId;
  protected final String executionId;
  protected final FlowElement flowElement;
  protected final Map<String, Object> executionVariables;
  protected final Map<String, Object> customPropertiesMap;

  public TransactionDependentExecutionListenerExecutionScope(TransactionDependentExecutionListener executionListener, String processInstanceId, String executionId,
                                                             FlowElement flowElement, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    this.processInstanceId = processInstanceId;
    this.executionId = executionId;
    this.executionListener = executionListener;
    this.flowElement = flowElement;
    this.executionVariables = executionVariables;
    this.customPropertiesMap = customPropertiesMap;
  }

  public TransactionDependentExecutionListener getExecutionListener() {
    return executionListener;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public FlowElement getFlowElement() {
    return flowElement;
  }

  public Map<String, Object> getExecutionVariables() {
    return executionVariables;
  }

  public Map<String, Object> getCustomPropertiesMap() {
    return customPropertiesMap;
  }
}
