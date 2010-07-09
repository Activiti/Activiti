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
package org.activiti.impl.db.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionDbImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.task.TaskImpl;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbExecutionImpl extends ExecutionImpl implements PersistentObject {

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

  protected boolean isNew = false;
  protected boolean isExecutionsInitialized = false;

  transient protected List<TaskImpl> tasks = null;

  DbExecutionImpl() {
  }

  public DbExecutionImpl(ProcessDefinitionDbImpl processDefinition) {
    super(processDefinition);
    this.isNew = true;
    this.executions = new ArrayList<ExecutionImpl>();
    this.isExecutionsInitialized = true;
    // Do not initialize variable map (let it happen lazily)

    CommandContext
      .getCurrent()
      .getPersistenceSession()
      .insert(this);
  }

  @Override
  protected ExecutionImpl newExecution() {
    DbExecutionImpl newExecution = new DbExecutionImpl();
    newExecution.isNew = true;
    newExecution.executions = new ArrayList<ExecutionImpl>();
    newExecution.isExecutionsInitialized = true;
    // Do not initialize variable map (let it happen lazily)

    CommandContext
      .getCurrent()
      .getPersistenceSession()
      .insert(newExecution);
    
    return newExecution;
  }
    
  // process definition ///////////////////////////////////////////////////////

  @Override
  public void ensureProcessDefinitionInitialized() {
    if ((processDefinition == null) && (processDefinitionId != null)) {
      setProcessDefinition(CommandContext.getCurrent().getPersistenceSession().findProcessDefinitionById(processDefinitionId));
    }
  }

  @Override
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    super.setProcessDefinition(processDefinition);
    this.processDefinitionId = processDefinition.getId();
  }

  // process instance /////////////////////////////////////////////////////////

  @Override
  public void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance = CommandContext.getCurrent().getPersistenceSession().findExecution(processInstanceId);
    }
  }

  @Override
  public void setProcessInstance(ExecutionImpl processInstance) {
    super.setProcessInstance(processInstance);
    if (processInstance != null) {
      this.processInstanceId = processInstance.getId();
    }
  }
  
  @Override
  protected boolean isProcessInstance() {
    return parentId == null;
  }

  // activity /////////////////////////////////////////////////////////////////

  @Override
  public void ensureActivityInitialized() {
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
  
  @Override
  public void ensureExecutionsInitialized() {
    // If the execution is new, then the child execution objects are already
    // fetched
    if (!isExecutionsInitialized) {
      this.executions = CommandContext.getCurrent().getPersistenceSession().findChildExecutions(getId());
      this.isExecutionsInitialized = true;
    }
  }

  // parent ///////////////////////////////////////////////////////////////////
  
  @Override
  public void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = CommandContext.getCurrent().getPersistenceSession().findExecution(parentId);
    }
  }

  @Override
  public void setParent(ExecutionImpl parent) {
    super.setParent(parent);

    if (parent != null) {
      this.parentId = parent.getId();
    } else {
      this.parentId = null;
    }
  }
  
  // customized persistence behaviour /////////////////////////////////////////

  @Override
  public void end() {
    super.end();

    ensureVariableMapInitialized();

    PersistenceSession persistenceSession = CommandContext.getCurrent().getPersistenceSession();

    Set<String> variableNames = new HashSet<String>(variableMap.getVariableNames());
    for (String variableName : variableNames) {
      variableMap.deleteVariable(variableName);
    }

    List<TaskImpl> tasks = persistenceSession.findTasksByExecution(id);
    for (TaskImpl task : tasks) {
      task.delete();
    }

    // then delete execution
    persistenceSession.delete(this);
  }

  // variables ////////////////////////////////////////////////////////////////
  
  @Override
  protected void ensureVariableMapInitialized() {
    if (variableMap==null) {
      ensureProcessDefinitionInitialized();
      this.variableMap = new DbVariableMap(this);
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
    persistentState.put("transition", this.transition);
    return persistentState;
  }

  // toString customization ///////////////////////////////////////////////////
  
  @Override
  protected String getIdForToString() {
    return id;
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
}
