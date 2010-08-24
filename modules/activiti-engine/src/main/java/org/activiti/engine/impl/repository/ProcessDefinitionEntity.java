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
package org.activiti.engine.impl.repository;

import java.util.ArrayList;

import org.activiti.engine.ProcessDefinition;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.VariableMap;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.runtime.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  protected String key;
  protected String name;
  protected int version;
  protected String deploymentId;
  protected String resourceName;
  
  public ProcessDefinitionEntity() {
    super(null);
  }

  public ExecutionEntity createProcessInstance() {
    ExecutionEntity processInstance = (ExecutionEntity) super.createProcessInstance();
    processInstance.setExecutions(new ArrayList<ExecutionImpl>());
    processInstance.setProcessDefinition(processDefinition);
    // Do not initialize variable map (let it happen lazily)

    // reset the process instance in order to have the db-generated process instance id available
    processInstance.setProcessInstance(processInstance);
    
    VariableMap variableMap = VariableMap.createNewInitialized(processInstance.getId(), processInstance.getId());
    processInstance.setVariables(variableMap);
    
    return processInstance;
  }
  
  @Override
  protected ExecutionImpl newProcessInstance() {
    ExecutionEntity processInstance = new ExecutionEntity();

    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(processInstance);

    return processInstance;
  }

  public String toString() {
    return "ProcessDefinitionEntity["+id+"]";
  }


  // getters and setters //////////////////////////////////////////////////////
  
  public Object getPersistentState() {
    return ProcessDefinitionEntity.class;
  }
  
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public int getVersion() {
    return version;
  }
  
  public void setVersion(int version) {
    this.version = version;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
}
