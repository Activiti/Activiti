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
package org.activiti.impl.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.Configuration;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionDbImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.repository.ProcessCache;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.tx.TransactionContext;


/**
 * @author Tom Baeyens
 */
public class ExecutionDbImpl extends ExecutionImpl implements PersistentObject {

  private static final long serialVersionUID = 1L;

  /** persisted reference to the processDefinition. 
   * @see #processDefinition 
   * @see #setProcessDefinition(ProcessDefinitionImpl)
   * @see #getProcessDefinition() */
  protected String processDefinitionId;

  /** persisted reference to the current position in the diagram within the {@link #processDefinition}. 
   * @see #activity 
   * @see #setActivity(ActivityImpl) 
   * @see #getActivity() */
  protected String activityId;
  
  protected boolean isExecutionsInitialized = false;

  transient protected List<TaskImpl> tasks = null;

  ExecutionDbImpl() {
  }
  
  ExecutionDbImpl(ProcessDefinitionDbImpl processDefinition) {
    super(processDefinition);
    this.isExecutionsInitialized = true;
  }
  
  public static ExecutionDbImpl createAndInsert(ProcessDefinitionDbImpl processDefinition) {
    ExecutionDbImpl processInstance = new ExecutionDbImpl(processDefinition);
    
    TransactionContext
      .getCurrent()
      .getTransactionalObject(PersistenceSession.class)
      .insert(processInstance);
    
    return processInstance;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    if ( (processDefinition==null)
         && (processDefinitionId!=null) 
       ) {
      ProcessCache processCache = 
        TransactionContext.getCurrent().getProcessEngine()
          .getConfigurationObject(Configuration.NAME_PROCESSCACHE, ProcessCache.class);

      processDefinition = processCache.findProcessDefinitionById(processDefinitionId);
    }
    return processDefinition;
  }
  
  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.processDefinitionId = processDefinition.getId();
  }

  public ActivityImpl getActivity() {
    if ( (activity==null)
        && (activityId!=null) 
       ) {
      activity = getProcessDefinition().findActivity(activityId);
    }
    return activity;
  }
  
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if (activity!=null) {
      this.activityId = activity.getId();
    } else {
      this.activityId = null;
    }
  }

  public void end() {
    super.end();
    delete();
  }

  protected void delete() {
    // cascade deletion to tasks
    List<TaskImpl> tasks = TransactionContext
        .getCurrent()
        .getTransactionalObject(PersistenceSession.class)
        .findTasksByExecution(id);
    for (TaskImpl task: tasks) {
      task.delete();
    }
    
    // then delete execution
    TransactionContext
        .getCurrent()
        .getTransactionalObject(PersistenceSession.class)
        .delete(this);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("activitiId", this.activityId);
    persistentState.put("isActive", this.isActive);
    persistentState.put("isConcurrencyScope", this.isConcurrencyScope);
    persistentState.put("transition", this.transition);
    return persistentState;
  }

  @Override
  public List< ? extends ExecutionImpl> getExecutions() {
    if (!isExecutionsInitialized) {
      // TODO initialize child executions from DB
    }
    return super.getExecutions();
  }
}
