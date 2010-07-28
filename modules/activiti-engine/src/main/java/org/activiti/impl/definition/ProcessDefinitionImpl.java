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
package org.activiti.impl.definition;

import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.variable.VariableTypes;
import org.activiti.pvm.ObjectProcessDefinition;

/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends ScopeElementImpl implements ObjectProcessDefinition, PersistentObject {
  
  private static final long serialVersionUID = 1L;

  protected String key;

  protected int version;

  protected DeploymentEntity deployment;
  
  /* Name of the resource that was used to deploy this processDefinition */
  transient protected String resourceName;

  private final VariableTypes variableTypes;

  public ProcessDefinitionImpl(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }
  
  public ExecutionImpl createProcessInstance() {
    ExecutionImpl execution = new ExecutionImpl(this);
    // TODO: maybe initialize variable declarations if needed;
    return execution;
  }
  
  public Object getPersistentState() {
    return ProcessDefinitionImpl.class;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public int getVersion() {
    return version;
  }
  public void setVersion(int version) {
    this.version = version;
  }
  public DeploymentEntity getDeployment() {
    return deployment;
  }
  public void setDeployment(DeploymentEntity deployment) {
    this.deployment = deployment;
  }
  public String getResourceName() {
    return resourceName;
  }
  public void setResourcename(String resourceName) {
    this.resourceName = resourceName;
  }
  public ProcessDefinitionImpl getProcessDefinition() {
    return this;
  }
  
  public VariableTypes getVariableTypes() {
    return variableTypes;
  }
}
