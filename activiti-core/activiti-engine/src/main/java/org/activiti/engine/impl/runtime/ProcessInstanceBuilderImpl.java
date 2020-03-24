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
package org.activiti.engine.impl.runtime;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;

/**


 */
public class ProcessInstanceBuilderImpl implements ProcessInstanceBuilder {

  protected RuntimeServiceImpl runtimeService;

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String messageName;
  protected String processInstanceName;
  protected String businessKey;
  protected String tenantId;
  protected Map<String, Object> variables;
  protected Map<String, Object> transientVariables;

  public ProcessInstanceBuilderImpl(RuntimeServiceImpl runtimeService) {
    this.runtimeService = runtimeService;
  }

  public ProcessInstanceBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ProcessInstanceBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public ProcessInstanceBuilder messageName(String messageName) {
    this.messageName = messageName;
    return this;
  }

  public ProcessInstanceBuilder name(String processInstanceName) {
    this.processInstanceName = processInstanceName;
    return this;
  }

  public ProcessInstanceBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public ProcessInstanceBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public ProcessInstanceBuilder variables(Map<String, Object> variables) {
    if (this.variables == null) {
      this.variables = new HashMap<String, Object>();
    }
    if (variables != null) {
      for (String variableName : variables.keySet()) {
        this.variables.put(variableName, variables.get(variableName));
      }
    }
    return this;
  }

  public ProcessInstanceBuilder variable(String variableName, Object value) {
    if (this.variables == null) {
      this.variables = new HashMap<String, Object>();
    }
    this.variables.put(variableName, value);
    return this;
  }

  public ProcessInstanceBuilder transientVariables(Map<String, Object> transientVariables) {
    if (this.transientVariables == null) {
      this.transientVariables = new HashMap<String, Object>();
    }
    if (transientVariables != null) {
      for (String variableName : transientVariables.keySet()) {
        this.transientVariables.put(variableName, transientVariables.get(variableName));
      }
    }
    return this;
  }

  public ProcessInstanceBuilder transientVariable(String variableName, Object value) {
    if (this.transientVariables == null) {
      this.transientVariables = new HashMap<String, Object>();
    }
    this.transientVariables.put(variableName, value);
    return this;
  }

  public ProcessInstance start() {
    return runtimeService.startProcessInstance(this);
  }

  public ProcessInstance create() {
    return runtimeService.createProcessInstance(this);
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getMessageName() {
    return messageName;
  }

  public String getProcessInstanceName() {
    return processInstanceName;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public Map<String, Object> getTransientVariables() {
    return transientVariables;
  }

}
