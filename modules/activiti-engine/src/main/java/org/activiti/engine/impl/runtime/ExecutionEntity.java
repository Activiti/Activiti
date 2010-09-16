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

package org.activiti.engine.impl.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.el.ELContext;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.process.ScopeImpl;
import org.activiti.pvm.impl.runtime.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class ExecutionEntity extends ExecutionImpl implements PersistentObject, Execution, ProcessInstance {

  private static final long serialVersionUID = 1L;
  
  private static Logger log = Logger.getLogger(ExecutionEntity.class.getName());

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
  
  protected ELContext cachedElContext;

  public ExecutionEntity() {
  }

  @Override
  protected ExecutionImpl newExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    newExecution.executions = new ArrayList<ExecutionImpl>();

    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(newExecution);

    return newExecution;
  }
  
  // scopes ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  @Override
  public void initialize() {
    log.fine("initializing "+this);

    ScopeImpl scope = getScope();
    ensureParentInitialized();

    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(BpmnParse.PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations!=null) {
      for (VariableDeclaration variableDeclaration : variableDeclarations) {
        variableDeclaration.initialize(this, parent);
      }
    }
    
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        BusinessCalendar businessCalendar = CommandContext.getCurrent().getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(
                DurationBusinessCalendar.NAME);
        Date duedate = businessCalendar.resolveDuedate(timerDeclaration.getDuedateDescription());

        TimerEntity timer = new TimerEntity(timerDeclaration);
        timer.setDuedate(duedate);
        timer.setExecution(this);

        CommandContext.getCurrent().getTimerSession().schedule(timer);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void destroy() {
    log.fine("destroying "+this);
    
    ScopeImpl scope = getScope();
    ensureParentInitialized();

    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(BpmnParse.PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations!=null) {
      for (VariableDeclaration variableDeclaration: variableDeclarations) {
        variableDeclaration.destroy(this, parent);
      }
    }

    if (variables!=null) {
      variables.clear();
    }

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations!=null) {
      CommandContext
        .getCurrent()
        .getTimerSession()
        .cancelTimers(this);
    }
    
    setScope(false);
  }

  protected ScopeImpl getScope() {
    ScopeImpl scope = null;
    if (isProcessInstance()) {
      scope = getProcessDefinition();
    } else {
      scope = getActivity();
    }
    return scope;
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
    if (executions==null) {
      this.executions = (List) CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findChildExecutionsByParentExecutionId(id);
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
  public void remove() {
    super.remove();

    // delete all the variable instances
    ensureVariablesInitialized();
    variables.clear();
    
    // TODO add cancellation of timers

    // delete all the tasks
    CommandContext commandContext = CommandContext.getCurrent();
    List<TaskEntity> tasks = (List) new TaskQueryImpl()
      .executionId(id)
      .executeList(commandContext, null);
    for (TaskEntity task : tasks) {
      if (replacedBy!=null) {
        task.setExecution(replacedBy);
      } else {
        task.delete();
      }
    }

    List<Job> jobs = new JobQueryImpl()
      .executionId(id)
      .executeList(commandContext, null);
    for (Job job: jobs) {
      if (replacedBy!=null) {
        ((JobEntity)job).setExecution((ExecutionEntity) replacedBy);
      } else {
        commandContext
          .getDbSqlSession()
          .delete(JobEntity.class, job.getId());
      }
    }

    // then delete execution
    commandContext
      .getDbSqlSession()
      .delete(ExecutionEntity.class, id);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void setReplacedBy(ExecutionImpl replacedBy) {
    super.setReplacedBy(replacedBy);
    
    // update the cached historic activity instances that are open
    CommandContext commandContext = CommandContext.getCurrent();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = dbSqlSession.findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
      if ( (cachedHistoricActivityInstance.getEndTime()==null)
           && (id.equals(cachedHistoricActivityInstance.getExecutionId())) 
         ) {
        cachedHistoricActivityInstance.setExecutionId(replacedBy.getId());
      }
    }
    
    // update the persisted historic activity instances that are open
    List<HistoricActivityInstanceEntity> historicActivityInstances = (List) new HistoricActivityInstanceQueryImpl()
      .executionId(id)
      .onlyOpen()
      .executeList(commandContext, null);
    for (HistoricActivityInstanceEntity historicActivityInstance: historicActivityInstances) {
      historicActivityInstance.setExecutionId(replacedBy.getId());
    }
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

  public int getRevisionNext() {
    return revision+1;
  }

  // toString customization ///////////////////////////////////////////////////
  
  public String getToStringIdentity() {
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
  public String getActivityId() {
    return activityId;
  }
  public String getSuperExecutionId() {
    return superExecutionId;
  }
}
