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

import java.util.List;

import org.activiti.impl.Jsonnable;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.json.JSONObject;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.pvm.ObjectProcessDefinition;

/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends ScopeElementImpl implements ObjectProcessDefinition, Jsonnable {
  
  private static final long serialVersionUID = 1L;

  protected String key;

  protected int version;

  protected DeploymentImpl deployment;
  
  protected ActivityImpl initial;
  
  protected boolean isNew = false;
  
  /* Name of the resource that was used to deploy this processDefinition */
  transient protected String resourceName; 
  
  public ExecutionImpl createProcessInstance() {
    ExecutionImpl execution = new ExecutionImpl(this);
    initializeVariableDeclarations(execution);
    return execution;
  }
  
  protected void initializeVariableDeclarations(ExecutionImpl execution) {
    for (VariableDeclarationImpl varDec: getVariableDeclarations()) {
      execution.createVariable(varDec.getName(), varDec.getType());
    }
  }

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
  public DeploymentImpl getDeployment() {
    return deployment;
  }
  public void setDeployment(DeploymentImpl deployment) {
    this.deployment = deployment;
  }
  public ActivityImpl getInitial() {
    return initial;
  }
  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }
  public boolean isNew() {
    return isNew;
  }
  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }
  public String getResourceName() {
    return resourceName;
  }
  public void setResourcename(String resourceName) {
    this.resourceName = resourceName;
  }
  
  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", id);
    if (key!=null) {
      jsonObject.put("key", key);
    }
    if (deployment!=null) {
      jsonObject.put("deploymentId", deployment.getId());
    }
    return jsonObject;
  }
}
