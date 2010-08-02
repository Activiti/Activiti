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

package org.activiti.engine.impl.persistence.runtime;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.runtime.ActivityInstanceImpl;
import org.activiti.pvm.impl.runtime.ProcessInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ProcessInstanceEntity extends ProcessInstanceImpl implements ProcessInstance, PersistentObject {
  
  protected String id;
  protected int revision;
  protected String processDefinitionId;
  protected VariableInstanceMap variableInstanceMap;

  public ProcessInstanceEntity() {
  }
  
  protected ProcessInstanceEntity(ProcessDefinitionImpl processDefinition) {
    super(processDefinition);
  }
  
  public static ProcessInstanceEntity createAndInsert(ProcessDefinitionImpl processDefinition) {
    ProcessInstanceEntity processInstance = new ProcessInstanceEntity(processDefinition);
    
    CommandContext
      .getCurrentSession(RuntimeSession.class)
      .insertProcessInstance(processInstance);
    
    processInstance.variableInstanceMap = new ProcessInstanceVariableMap(processInstance);
    
    return processInstance;
  }
  
  public void delete() {
    for (ActivityInstanceImpl activityInstance: getActivityInstances()) {
      ((ActivityInstanceEntity)activityInstance).delete();
    }
    for (VariableInstanceEntity variableInstance: getVariableInstanceMap().getVariableInstances().values()) {
      variableInstance.delete();
    }
  }
  
  public VariableInstanceMap getVariableInstanceMap() {
    if (variableInstanceMap==null) {
      variableInstanceMap = new ProcessInstanceVariableMap(this);
    }
    return variableInstanceMap;
  }
  

  public Object getPersistentState() {
    throw new ActivitiException("not yet implemented");
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
}
