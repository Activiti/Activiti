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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;

import org.activiti.engine.Execution;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.runtime.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class ExecutionEntity extends ExecutionImpl implements PersistentObject, Execution, ProcessInstance {

  private static final long serialVersionUID = 1L;

  protected String id = null;
  protected int revision = 1;

  /**
   * persisted reference to the processDefinition.
   * 
   * @see #processDefinition
   * @see #setProcessDefinition(ProcessDefinitionImpl)
   * @see #getProcessDefinition()
   */
  protected String processDefinitionId;

  /**
   * persisted reference to the current position in the diagram within the
   * {@link #processDefinition}.
   * 
   * @see #activity
   * @see #setActivity(ActivityImpl)
   * @see #getActivity()
   */
  protected String activityId;

  /**
   * persisted reference to the process instance.
   * 
   * @see #getProcessInstance()
   */
  protected String processInstanceId;

  /**
   * persisted reference to the parent of this execution.
   * 
   * @see #getParent()
   * @see #setParent(ExecutionImpl)
   */
  protected String parentId;
  
  /**
   * persisted reference to the super execution of this execution
   * 
   * @See {@link #getSuperExecution()}
   * @see #setSuperExecution(ExecutionImpl)
   */
  protected String superExecutionId;
  
  protected boolean isNew = false;
  protected boolean isExecutionsInitialized = false;

  protected ELContext cachedElContext;

  ExecutionEntity() {
  }

  public ExecutionEntity(ProcessDefinitionEntity processDefinition) {
    super(processDefinition);
    this.isNew = true;
    this.executions = new ArrayList<ExecutionImpl>();
    this.isExecutionsInitialized = true;
    // Do not initialize variable map (let it happen lazily)

    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(this);

    // reset the process instance in order to have the db-generated process instance id available
    setProcessInstance(this);
    
    this.variables = VariableMap.createNewInitialized(id, processInstanceId);
  }

  @Override
  protected ExecutionImpl newExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    newExecution.isNew = true;
    newExecution.executions = new ArrayList<ExecutionImpl>();
    newExecution.isExecutionsInitialized = true;
    // Do not initialize variable map (let it happen lazily)

    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(newExecution);

    return newExecution;
  }
    
  // process definition ///////////////////////////////////////////////////////

  @Override
  protected void ensureProcessDefinitionInitialized() {
    if ((processDefinition == null) && (processDefinitionId != null)) {
      setProcessDefinition(CommandContext.getCurrent().getRepositorySession().findDeployedProcessDefinitionById(processDefinitionId));
    }
  }

  @Override
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    super.setProcessDefinition(processDefinition);
    this.processDefinitionId = processDefinition.getId();
  }

  // process instance /////////////////////////////////////////////////////////

  @Override
  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance = CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findExecutionById(processInstanceId);
    }
  }

  @Override
  public void setProcessInstance(ExecutionImpl processInstance) {
    super.setProcessInstance(processInstance);
    if (processInstance != null) {
      this.processInstanceId = ((ExecutionEntity)processInstance).getId();
    }
  }
  
  @Override
  public boolean isProcessInstance() {
    return parentId == null;
  }

  // activity /////////////////////////////////////////////////////////////////

  @Override
  protected void ensureActivityInitialized() {
    if ((activity == null) && (activityId != null)) {
      activity = getProcessDefinition().findActivity(activityId);
    }
  }

  @Override
  public void setActivity(ActivityImpl activity) {
    super.setActivity(activity);
    if (activity != null) {
      this.activityId = activity.getId();
    } else {
      this.activityId = null;
    }
  }
  
  // executions ///////////////////////////////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  @Override
  protected void ensureExecutionsInitialized() {
    // If the execution is new, then the child execution objects are already
    // fetched
    if (!isExecutionsInitialized) {
      this.executions = (List) CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findChildExecutionsByParentExecutionId(id);
      this.isExecutionsInitialized = true;
    }
  }

  // parent ///////////////////////////////////////////////////////////////////
  
  @Override
  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findExecutionById(parentId);
    }
  }

  @Override
  public void setParent(ExecutionImpl parent) {
    super.setParent(parent);

    if (parent != null) {
      this.parentId = ((ExecutionEntity)parent).getId();
    } else {
      this.parentId = null;
    }
  }
  
  // super- and subprocess executions /////////////////////////////////////////
  
  @Override
  protected void ensureSuperExecutionInitialized() {
    if (superExecution == null && superExecutionId != null) {
      superExecution = CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findExecutionById(superExecutionId);
    }
  }
  
  @Override
  public void setSuperExecution(ExecutionImpl superExecution) {
    super.setSuperExecution(superExecution);
    
    if (superExecution != null) {
      this.superExecutionId = ((ExecutionEntity)superExecution).getId();
    } else {
      this.superExecutionId = null;
    }
  }
  
  @Override
  protected void ensureSubProcessInstanceInitialized() {
    if (subProcessInstance == null) {
      subProcessInstance = CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findSubProcessInstanceBySuperExecutionId(id);
    }
  }
  
  // customized persistence behaviour /////////////////////////////////////////

  @SuppressWarnings("unchecked")
  @Override
  public void end() {
    super.end();

    ensureVariablesInitialized();

    // delete all the variable instances
    variables.clear();
    
    // TODO add cancellation of timers

    // delete all the tasks
    List<TaskEntity> tasks = (List) new TaskQueryImpl()
      .executionId(id)
      .executeList(CommandContext.getCurrent(), null);
    for (TaskEntity task : tasks) {
      task.delete();
    }

    // then delete execution
    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .delete(this);
  }

  // variables ////////////////////////////////////////////////////////////////
  
  @Override
  protected void ensureVariablesInitialized() {
    if (variables==null) {
      this.variables = new VariableMap(id, processInstanceId);
    }
  }
  
  // persistent state /////////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("activitiId", this.activityId);
    persistentState.put("isActive", this.isActive);
    persistentState.put("isConcurrent", this.isConcurrent);
    persistentState.put("isScope", this.isScope);
    persistentState.put("parentId", parentId);
    persistentState.put("superExecution", this.superExecutionId);
    return persistentState;
  }

  // toString customization ///////////////////////////////////////////////////
  
  @Override
  protected String getIdForToString() {
    return id;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getParentId() {
    return parentId;
  }
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
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
  public boolean isNew() {
    return isNew;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
}
