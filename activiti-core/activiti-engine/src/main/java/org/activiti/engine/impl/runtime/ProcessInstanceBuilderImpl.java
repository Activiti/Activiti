/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.runtime.ProcessInstanceBuilder;

/**
 * Builder to create a new ProcessInstance.
 *
 * processDefinitionId or processDefinitionKey should always be set.
 */
@Internal
public class ProcessInstanceBuilderImpl implements ProcessInstanceBuilder {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String messageName;
  protected String processInstanceName;
  protected String businessKey;
  protected String tenantId;
  protected Map<String, Object> variables;
  protected Map<String, Object> transientVariables;

  public static ProcessInstanceBuilder newProcessInstanceBuilder() {
    return new ProcessInstanceBuilderImpl();
  }

  /**
   * Set the id of the process definition
   **/
  public ProcessInstanceBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  /**
   * Set the key of the process definition, latest version of the process definition with the given key.
   * If processDefinitionId was set this will be ignored
   **/
  public ProcessInstanceBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  /**
   * Set the message name that needs to be used to look up the process definition that needs to be used to start the process instance.
   */
  public ProcessInstanceBuilder messageName(String messageName) {
    this.messageName = messageName;
    return this;
  }

  /**
   * Set the name of process instance
   **/
  public ProcessInstanceBuilder name(String processInstanceName) {
    this.processInstanceName = processInstanceName;
    return this;
  }

  /**
   * Set the businessKey of process instance
   **/
  public ProcessInstanceBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  /**
   * Set the tenantId of process instance
   **/
  public ProcessInstanceBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Sets the process variables
   */
  public ProcessInstanceBuilder variables(Map<String, Object> variables) {
    if (this.variables == null) {
      this.variables = new HashMap<>();
    }
    if (variables != null) {
      for (String variableName : variables.keySet()) {
        this.variables.put(variableName, variables.get(variableName));
      }
    }
    return this;
  }

  /**
   * Adds a variable to the process instance
   **/
  public ProcessInstanceBuilder variable(String variableName, Object value) {
    if (this.variables == null) {
      this.variables = new HashMap<>();
    }
    this.variables.put(variableName, value);
    return this;
  }

  /**
   * Sets the transient variables
   */
  public ProcessInstanceBuilder transientVariables(Map<String, Object> transientVariables) {
    if (this.transientVariables == null) {
      this.transientVariables = new HashMap<>();
    }
    if (transientVariables != null) {
      for (String variableName : transientVariables.keySet()) {
        this.transientVariables.put(variableName, transientVariables.get(variableName));
      }
    }
    return this;
  }

  /**
   * Adds a transient variable to the process instance
   */
  public ProcessInstanceBuilder transientVariable(String variableName, Object value) {
    if (this.transientVariables == null) {
      this.transientVariables = new HashMap<>();
    }
    this.transientVariables.put(variableName, value);
    return this;
  }

  public boolean hasProcessDefinitionIdOrKey() {
    return this.getProcessDefinitionId() != null || this.getProcessDefinitionKey() != null;
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
