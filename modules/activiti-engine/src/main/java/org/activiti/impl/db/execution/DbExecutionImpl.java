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
    // Do not initialize variable map (let it happen lazily)
  }

  DbExecutionImpl(ProcessDefinitionDbImpl processDefinition) {
    super(processDefinition);
    this.isNew = true;
    this.isExecutionsInitialized = true;
    // Do not initialize variable map (let it happen lazily)
  }

  DbExecutionImpl(ExecutionImpl parent) {
    super(parent);
    this.isNew = true;
    this.isExecutionsInitialized = true;
    // Do not initialize variable map (let it happen lazily)
  }

  public static DbExecutionImpl createAndInsert(ProcessDefinitionDbImpl processDefinition) {
    DbExecutionImpl processInstance = new DbExecutionImpl(processDefinition);
    CommandContext.getCurrent().getPersistenceSession().insert(processInstance);
    processInstance.setProcessInstance(processInstance);
    return processInstance;
  }

  public static DbExecutionImpl createAndInsert(ExecutionImpl parent) {
    DbExecutionImpl childExecution = new DbExecutionImpl(parent);

    CommandContext.getCurrent().getPersistenceSession().insert(childExecution);

    childExecution.setProcessInstance(parent.getProcessInstance());

    return childExecution;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    if ((processDefinition == null) && (processDefinitionId != null)) {
      setProcessDefinition(CommandContext.getCurrent().getPersistenceSession().findProcessDefinitionById(processDefinitionId));
    }
    return processDefinition;
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Override
  public void setProcessInstance(ExecutionImpl processInstance) {
    super.setProcessInstance(processInstance);
    if (processInstance != null) {
      this.processInstanceId = processInstance.getId();
    }
  }

  @Override
  public ExecutionImpl getProcessInstance() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance = CommandContext.getCurrent().getPersistenceSession().findExecution(processInstanceId);
    }
    return processInstance;
  }

  @Override
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    super.setProcessDefinition(processDefinition);
    this.processDefinitionId = processDefinition.getId();
  }

  @Override
  public ActivityImpl getActivity() {
    if ((activity == null) && (activityId != null)) {
      activity = getProcessDefinition().findActivity(activityId);
    }
    return activity;
  }

  @Override
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if (activity != null) {
      this.activityId = activity.getId();
    } else {
      this.activityId = null;
    }
  }

  @Override
  public ExecutionImpl getParent() {
    if (parent == null && parentId != null) {
      parent = CommandContext.getCurrent().getPersistenceSession().findExecution(parentId);
    }
    return parent;
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

  @Override
  protected boolean isProcessInstance() {
    return parentId == null;
  }

  @Override
  public void end() {
    super.end();
    delete();
  }

  protected void delete() {

    if (variableMap == null) {
      initializeVariableMap();
    }

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

  @Override
  public List< ? extends ExecutionImpl> getExecutions() {
    // If the execution is new, then the child execution objects are already
    // fetched
    if (!isExecutionsInitialized) {
      this.executions = CommandContext.getCurrent().getPersistenceSession().findChildExecutions(getId());
      this.isExecutionsInitialized = true;
    }
    return executions;
  }

  @Override
  protected ExecutionImpl createChildExecution() {
    return createAndInsert(this);
  }

  @Override
  protected void initializeVariableMap() {
    if (processDefinition == null) {
      getProcessDefinition();
    }
    this.variableMap = new DbVariableMap(this);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("activitiId", this.activityId);
    persistentState.put("isActive", this.isActive);
    persistentState.put("isConcurrencyScope", this.isConcurrencyScope);
    persistentState.put("parentId", parentId);
    persistentState.put("transition", this.transition);
    return persistentState;
  }

  // Need to overwrite the default toString(),
  // since that one could fetch data from the DB, which
  // causes unnessary loadings.
  public String toString() {
    return "ProcInst[id=" + getId() + " at activity " + activityId + "]";
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
}
