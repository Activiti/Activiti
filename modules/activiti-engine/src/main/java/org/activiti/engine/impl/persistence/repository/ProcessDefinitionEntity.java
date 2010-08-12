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
package org.activiti.engine.impl.persistence.repository;

import org.activiti.engine.ProcessDefinition;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  protected String key;
  protected String name;
  protected int version;
  protected String deploymentId;
  
  public ProcessDefinitionEntity() {
    super(null);
  }

  public ExecutionEntity createProcessInstance() {
    return ExecutionEntity.createProcessInstance(this);
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
}
