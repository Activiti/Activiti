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
package org.activiti.cdi;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.annotation.ProcessInstanceId;
import org.activiti.cdi.annotation.TaskId;
import org.activiti.cdi.impl.context.BusinessProcessAssociationManager;
import org.activiti.cdi.impl.context.CachingBeanStore;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * Bean supporting contextual business process management. This allows us to 
 * implement a unit of work, in which a particular CDI scope (Conversation / 
 * Request / Thread) is associated with a particular Execution / ProcessInstance 
 * or Task.
 * <p />
 * The protocol is that we <em>associate</em> the {@link BusinessProcess} bean 
 * with a particular Execution / Task, then perform some changes (retrieve / set process 
 * variables) and then end the unit of work. This bean makes sure that our changes are 
 * only "flushed" to the process engine when we successfully complete the unit of work.
 * <p />
 * A typical usage scenario might look like this:<br /> 
 * <strong>1st unit of work ("process instantiation"):</strong>
 * <pre>
 * conversation.begin();
 * ...
 * businessProcess.setVariable("billingId", "1"); // setting variables before starting the process 
 * businessProcess.startProcessByKey("billingProcess");
 * conversation.end();
 * </pre>
 * <strong>2nd unit of work ("perform a user task"):</strong>
 * <pre>
 * conversation.begin();
 * businessProcess.startTask(id); // now we have associated a task with the current conversation
 * ...                            // this allows us to retrieve and change process variables  
 *                                // and @BusinessProcessScoped beans
 * businessProcess.setVariable("billingDetails", "someValue"); // these changes are cached in the conversation
 * ...
 * businessProcess.completeTask(); // now all changed process variables are flushed
 * conversation.end(); 
 * </pre>
 * <p />
 * <strong>NOTE:</strong> in the absence of a conversation, (non faces request, i.e. when processing a JAX-RS, 
 * JAX-WS, JMS, remote EJB or plain Servlet requests), the {@link BusinessProcess} bean associates with the 
 * current Request (see {@link RequestScoped @RequestScoped}).
 * <p />
 * <strong>NOTE:</strong> in the absence of a request, ie. when the activiti JobExecutor accesses 
 * {@link BusinessProcessScoped @BusinessProcessScoped} beans, the execution is associated with the 
 * current thread. 
 * 
 * @author Daniel Meyer
 * 
 */
@Named 
public class BusinessProcess implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private static Logger logger = Logger.getLogger(BusinessProcess.class.getName());

  @Inject private ProcessEngine processEngine;

  @Inject private BusinessProcessAssociationManager associationManager;
  
  @Inject private Instance<Conversation> conversationInstance;

  public ProcessInstance startProcessById(String processDefinitionId) {        
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, getBeanStore().getAll());
    associate(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessById(String processDefinitionId, Map<String, Object> variables) {
    getBeanStore().putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, getBeanStore().getAll());
    associate(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByKey(String key) {
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, getBeanStore().getAll());
    associate(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByKey(String key, Map<String, Object> variables) {
    getBeanStore().putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, getBeanStore().getAll());
    associate(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByName(String string) {
    ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionName(string).singleResult();
    if (definition == null) {
      logger.log(Level.SEVERE, "No process definition found for name: " + string);
      throw new ActivitiException("No process definition found for name: " + string);
    }
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(definition.getId(), getBeanStore().getAll());
    associate(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByName(String string, Map<String, Object> variables) {
    ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionName(string).singleResult();
    if (definition == null) {
      logger.log(Level.SEVERE, "No process definition found for name: " + string);
      throw new ActivitiException("No process definition found for name: " + string);
    }
    getBeanStore().putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(definition.getId(), getBeanStore().getAll());
    associate(instance.getProcessInstanceId());
    return instance;
  }

  /**
   * Associate with the provided execution. This starts a unit of work.
   * 
   * @param executionId
   *          the id of the execution to associate with.
   * @throw ActivitiCdiException
   *          if no such execution exists
   */
  public void associateExecutionById(String executionId) {
    associate(executionId);
    try {
      getExecution();
    } catch (ActivitiException e) {
      associationManager.disAssociate();
      throw new ActivitiCdiException("Cannot resume process: no execution with id '" + executionId + "' found.");
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Resumig Execution[" + executionId + "].");
    }
  }
  
  /**
   * returns true if an {@link Execution} is associated.
   * 
   * @see #associateExecutionById(String)
   */
  public boolean isAssociated() {
    return associationManager.getExecutionId() != null;
  }
  
  /**
   * Signals the current execution, see {@link RuntimeService#signal(String)}
   * <p/>
   * Ends the current unit of work (flushes changes to process variables set
   * using {@link #setVariable(String, Object)} or made on
   * {@link BusinessProcessScoped @BusinessProcessScoped} beans).
   * 
   * @throws ActivitiCdiException
   *           if no execution is currently associated
   * @throws ActivitiException
   *           if the activiti command fails
   */
  public void signalExecution() {
    assertAssociated();
    processEngine.getRuntimeService().signal(associationManager.getExecutionId(), associationManager.getBeanStore().getAllAndClear());
    associationManager.disAssociate();
  }
  
  /**
   * @see #signalExecution()
   * 
   * In addition, this method allows to end the current conversation
   */
  public void signalExecution(boolean endConversation) {
    signalExecution();
    if(endConversation) {
      conversationInstance.get().end();
    }
  }

  // -------------------------------------

  /**
   * Associates the task with the provided taskId with the current conversation.
   * <p/>
   * 
   * @param taskId
   *          the id of the task
   * 
   * @return the resumed task
   * 
   * @throws ActivitiCdiException
   *           if no such task is found
   */
  public Task startTask(String taskId) {
    Task currentTask = associationManager.getTask();
    if(currentTask != null && currentTask.getId().equals(taskId)) {
      return currentTask;
    }
    Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if(task == null) {
      throw new ActivitiCdiException("Cannot resume task with id '"+taskId+"', no such task.");
    }
    associationManager.setTask(task);
    associateExecutionById(task.getExecutionId());     
    return task;
  }
  
  /**
   * @see #startTask(String) 
   * 
   * this method allows to start a conversation if no conversation is active
   */
  public Task startTask(String taskId, boolean beginConversation) {
    if(beginConversation) {
      Conversation conversation = conversationInstance.get();
      if(conversation.isTransient()) {
       conversation.begin(); 
      }
    }
    return startTask(taskId);
  }

  /**
   * Completes the current UserTask, see {@link TaskService#complete(String)}
   * <p/>
   * Ends the current unit of work (flushes changes to process variables set
   * using {@link #setVariable(String, Object)} or made on
   * {@link BusinessProcessScoped @BusinessProcessScoped} beans).
   * 
   * @throws ActivitiCdiException
   *           if no task is currently associated
   * @throws ActivitiException
   *           if the activiti command fails
   */
  public void completeTask() {
    assertTaskAssociated();
    processEngine.getTaskService().complete(getTask().getId(), associationManager.getBeanStore().getAllAndClear());
    associationManager.disAssociate();
  }
  
  /**
   * @see BusinessProcess#completeTask()
   * 
   * In addition this allows to end the current conversation.
   * 
   */
  public void completeTask(boolean endConversation) {
    completeTask();
    if(endConversation) {
      conversationInstance.get().end();
    }
  }

  public boolean isTaskAssociated() {
    return associationManager.getTask() != null;
  }
 
  // -------------------------------------------------

  /**
   * @param variableName
   *          the name of the process variable for which the value is to be
   *          retrieved
   * @return the value of the provided process variable or 'null' if no such
   *         variable is set
   */
  @SuppressWarnings("unchecked")
  public <T> T getVariable(String variableName) {
    resumeExecutionFromContext();
    Object value = null;    
    if (!isAssociated() || getBeanStore().holdsValue(variableName)) {
      value = getBeanStore().getContextualInstance(variableName);
    } else {
      if(Context.getCommandContext() != null) {      
        value = Context.getExecutionContext().getExecution().getVariable(variableName);       
      }else {
        value = processEngine.getRuntimeService().getVariable(associationManager.getExecutionId(), variableName);   
      }
      // cache the value in the bean store.
      getBeanStore().put(variableName, value);
    }
    setVariable(variableName, value);
    if (value == null) {
      return null;
    } else {      
      return (T) value;
    }
  }

  /**
   * Set a value for a process variable.
   * <p />
   * 
   * <strong>NOTE:</strong> If no execution is currently associated, 
   * the value is temporarily cached and flushed to the process instance 
   * at the end of the unit of work
   * 
   * @param variableName
   *          the name of the process variable for which a value is to be set
   * @param value
   *          the value to be set
   * 
   */
  public void setVariable(String variableName, Object value) {
    resumeExecutionFromContext();    
    // write through to the execution, if participating in a command:
    if(Context.getCommandContext() != null) {
      ExecutionEntity execution = Context.getExecutionContext().getExecution();
      if(execution != null) {
        execution.setVariable(variableName, value);
      }
    }
    getBeanStore().put(variableName, value);   
  }
  
  // ----------------------------------- Getters / Setters and Producers

  /**
   * @see #startTask(String)
   */
  public void setTask(Task task) {
    startTask(task.getId());    
  }
  
  /**
   * @see #startTask(String)
   */
  public void setTaskId(String taskId) {
    startTask(taskId);    
  }
  
  /**
   * @see #associateExecutionById(String)
   */
  public void setExecution(Execution execution) {
    associate(execution.getId());
  }
  
  /**
   * @see #associateExecutionById(String)
   */
  public void setExecutionId(String executionId) {
    associate(executionId);
  }

  /**
   * Returns the id of the currently associated process instance or 'null'
   */
  /* Also makes the processId available for injection */
  @Produces @Named("processInstanceId") @ProcessInstanceId public String getProcessInstanceId() {
    ProcessInstance processInstance = getProcessInstance();
    if(processInstance != null) {
      return processInstance.getId();
    }
    return null;
  }

  /**
   * Returns the id of the task associated with the current conversation or 'null'.
   */
  /* Also makes the taskId available for injection */
  @Produces @Named("taskId") @TaskId public String getTaskId() {
    Task task = getTask();
    if(task != null) {
      return task.getId();
    }
    return null;
  }

  /**
   * Returns the currently associated {@link Task}  or 'null'
   * 
   * @throws ActivitiCdiException
   *           if no {@link Task} is associated. Use {@link #isTaskAssociated()}
   *           to check whether an association exists.
   * 
   */
  /* Also makes the current Task available for injection */
  @Produces @Named public Task getTask() {
    return associationManager.getTask();
  }
  
  /**
   * Returns the currently associated execution  or 'null'
   * 
   */
  /* Also makes the current Execution available for injection */
  @Produces @Named public Execution getExecution() {    
    // participate in current command:
    if(Context.getCommandContext() != null) {
      ExecutionEntity execution = Context.getExecutionContext().getExecution();
      if(execution != null) {
        return execution;
      }
    }
    if(isAssociated()) {
      return processEngine.getRuntimeService()
            .createExecutionQuery()
            .executionId(associationManager.getExecutionId())
            .singleResult();    
    }
    return null;
  }
  
  /**
   * @see #getExecution()
   */
  @Produces @Named public String getExecutionId() {
    Execution e = getExecution();
    return e != null ? e.getId() : null;
  }

  /**
   * Returns the {@link ProcessInstance} currently associated or 'null'
   * 
   * @throws ActivitiCdiException
   *           if no {@link Execution} is associated. Use
   *           {@link #isAssociated()} to check whether an association exists.
   */
  /* Also makes the current ProcessInstance available for injection */
  @Produces @Named public ProcessInstance getProcessInstance() {
    // participate in current command:
    if(Context.getCommandContext() != null) {
      ExecutionEntity processInstance = Context.getExecutionContext().getProcessInstance();
      if(processInstance != null) {
        return processInstance;
      }
    }
    Execution execution = getExecution();    
    if(execution != null){
      return processEngine
            .getRuntimeService()
            .createProcessInstanceQuery()
            .processInstanceId(execution.getProcessInstanceId())
            .singleResult();
    }
    return null;    
  }
   
  // internal implementation //////////////////////////////////////////////////////////

  protected void assertAssociated() {
    resumeExecutionFromContext();
    if (associationManager.getExecutionId() == null) {
      throw new ActivitiCdiException("No execution associated. Call busniessProcess.associateExecutionById() or businessProcess.startTask() first.");
    }
  }

  protected void assertTaskAssociated() {
    if (associationManager.getTask() == null) {
      throw new ActivitiCdiException("No task associated. Call businessProcess.startTask() first.");
    }
  }
  
  protected void associate(String executionId) {
    associationManager.associate(executionId);
  }

  protected CachingBeanStore getBeanStore() {
   return associationManager.getBeanStore();
  }

  /**
   * if no association exists, we try to resume an execution form the activiti 
   * execution context (possible if we participate in a command)
   */
  protected void resumeExecutionFromContext() {
    if (associationManager.getExecutionId() != null) {
      return;
    }
    if (Context.getCommandContext() != null) {
      Execution execution = Context.getExecutionContext().getExecution();
      if (execution != null) {
        associationManager.associate(execution.getId());
      }
    }
  }
 
}
