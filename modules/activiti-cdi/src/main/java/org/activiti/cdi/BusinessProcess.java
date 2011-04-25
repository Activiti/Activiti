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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.annotations.ProcessId;
import org.activiti.cdi.annotations.TaskId;
import org.activiti.cdi.impl.AbstractProcessResuming;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * Represents the contextual business process instance. Holds the
 * ids of the current process instance and task. ProcessInstances can be
 * associated using {@link #resumeProcessById(String)}
 * <p />
 * Alternatively, this implementation resumes a ProcessInstance bound to the
 * current thread. For example, if a process is started using
 * {@link RuntimeService#startProcessInstanceByKey(String)}, and it calls a
 * cdi-bean (through EL) and the bean calls {@link #getProcessVariable(String)},
 * the variable is looked up for the current process instance. NOTE: this only
 * works with local ProcessEngines.
 * 
 * @author Daniel Meyer
 * 
 */
@Named 
public class BusinessProcess extends AbstractProcessResuming implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected Logger logger = Logger.getLogger(BusinessProcess.class.getName());

  @Inject BeanManager beanManager;

  @Inject ProcessEngine processEngine;

  @Inject Actor actor;
  
  /*
   * TODO: Discuss/think about whether to provide the start* methods here: an
   * alternative would be to proxy the RuntimeService (?)
   */

  public ProcessInstance startProcessById(String processDefinitionId) {
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, getBeanStore().getAll());
    associateBusinessProcessInstance(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessById(String processDefinitionId, Map<String, Object> variables) {
    getBeanStore().putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, getBeanStore().getAll());
    associateBusinessProcessInstance(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByKey(String key) {
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, getBeanStore().getAll());
    associateBusinessProcessInstance(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByKey(String key, Map<String, Object> variables) {
    getBeanStore().putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, getBeanStore().getAll());
    associateBusinessProcessInstance(instance.getProcessInstanceId());
    return instance;
  }

  public ProcessInstance startProcessByName(String string) {
    ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionName(string).singleResult();
    if (definition == null) {
      logger.log(Level.SEVERE, "No process definition found for name: " + string);
      throw new ActivitiException("No process definition found for name: " + string);
    }
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(definition.getId(), getBeanStore().getAll());
    associateBusinessProcessInstance(instance.getProcessInstanceId());
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
    associateBusinessProcessInstance(instance.getProcessInstanceId());
    return instance;
  }

  /**
   * Associate the process instance with the provided processId with the current
   * conversation.
   * 
   * @param processInstanceId
   *          the id of the process instance to be resumed.
   */
  public void resumeProcessById(String processInstanceId) {
    associateBusinessProcessInstance(processInstanceId);
    try {
      getProcessInstance();
    } catch (ActivitiException e) {
      associationManager.disAssociateProcessInstance();
      throw new ActivitiException("Cannot resume process: no ProcessInstance with id '" + processInstanceId + "' found.");
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Resumig ProcessInstance[" + processInstanceId + "].");
    }
  }


  // -------------------------------------
  // discuss / think about whether to provide the task methods here. An
  // alternative would be to proxy / wrap the task service

  /**
   * Associates the task with the provided taskId with the current conversation.
   * 
   * @param the
   *          id of the task
   * @return the resumed task
   * @throws ActivitiException
   *           if no such task is found
   */
  public Task resumeTaskById(String taskId) {
    associateTaskInstance(taskId);
    Task task = getTask();
    if (task == null) {
      associationManager.disAssociateTask();
      throw new ActivitiException("No task with id '" + taskId + "' found.");
    }
    resumeProcessById(task.getProcessInstanceId());
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Resumig Task[" + taskId + "].");
    }
    return task;
  }

  /**
   * Complete the task associated with the current conversation.
   */
  public void completeTask() {
    assertTaskIdSet();
    processEngine.getTaskService().complete(associationManager.getTaskId());
    associationManager.disAssociateTask();
  }

  public boolean hasTask() {
    resumeTask();
    return associationManager.getTaskId() != null;
  }

  // -------------------------------------------------
  // process variables (TODO: move to separate bean ?)

  /**
   * @param variableName
   *          the name of the process variable for which the value is to be
   *          retrieved
   * @return the value of the provided process variable or 'null' if no such
   *         variable is set
   */
  public Object getProcessVariable(String variableName) {
    return getProcessVariable(variableName, Object.class);
  }

  /**
   * @param variableName
   *          the name of the process variable for which the value is to be
   *          retrieved
   * @return the value of the provided process variable or 'null' if no such
   *         variable is set
   */
  @SuppressWarnings("unchecked")
  public <T> T getProcessVariable(String variableName, Class<T> ofClazz) {
    resumeProcess();
    Object value = null;
    
    if (!isActive() || 
              // TODO: think about concurrent processes
              getBeanStore().holdsValue(variableName) 
       ) {
      value = getBeanStore().getContextualInstance(variableName);
    } else {
      try {
        // this allows us to retrieve process variables before the first
        // transaction is completed.
        value = Context.getExecutionContext().getExecution().getVariable(variableName);
      } catch (Exception e) {
        value = processEngine.getRuntimeService().getVariable(associationManager.getProcessInstanceId(), variableName);
      }
      getBeanStore().put(variableName, value);
    }
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
   * <strong>NOTE:</strong> If no processInstance is associated with the current
   * conversation, the value is added to a temporary conversational bean store
   * and flushed to the process instance at process instantiation time.
   * 
   * @param variableName
   *          the name of the process variable for which a value is to be set
   * @param value
   *          the value to be set
   * 
   */
  public void setProcessVariable(String variableName, Object value) {
    resumeProcess();
    // this allows us to flush the bean store to update the values of changed beans when a task is completed:
    getBeanStore().put(variableName, value);
    if (isActive()) {
      setProcessVariableInExecution(variableName,value);
    }
  }
  
  protected void setProcessVariableInExecution(String variableName, Object value) {
    try {
      // this allows us to store process variables before the first
      // transaction is completed (RuntimeService.setVariable(...) does not work then
      Context.getExecutionContext().getExecution().setVariable(variableName, value);
    } catch (Exception e) {
      processEngine.getRuntimeService().setVariable(associationManager.getProcessInstanceId(), variableName, value);
    }
  }
  
  // ----------------------------------- Getters / Setters and Producers

  public void setProcessInstanceId(String processInstanceId) {
    resumeProcessById(processInstanceId);
  }

  public void setTaskId(String taskId) {
    resumeTaskById(taskId);
  }

  /**
   * Returns the id of the process instance associated with the current conversation or 'null'.
   */
  /* Also makes the processId available for injection */
  @Produces @Named("processId") @ProcessId public String getProcessInstanceId() {
    resumeProcess();
    return associationManager.getProcessInstanceId();
  }

  /**
   * Returns the id of the task associated with the current conversation or 'null'.
   */
  /* Also makes the taskId available for injection */
  @Produces @Named("taskId") @TaskId public String getTaskId() {
    resumeTask();
    return associationManager.getTaskId();
  }

  /**
   * Returns the {@link Task} associated with the current
   * conversation
   * 
   * @throws ActivitiException
   *           if no task is associated with the current
   *           conversation. Use {@link #hasTask()} to check whether this
   *           conversation is associated with a task.
   */
  /* Also makes the current Task available for injection */
  @Produces @Named public Task getTask() {
    assertTaskIdSet();
    return processEngine
      .getTaskService()
      .createTaskQuery()
      .taskId(associationManager.getTaskId())
      .singleResult();
  }

  /**
   * Returns the {@link ProcessInstance} associated with the current
   * conversation
   * 
   * @throws ActivitiException
   *           if no processInstance is associated with the current
   *           conversation. Use {@link #hasProcess()} to check whether this
   *           conversation is associated with a process instance.
   */
  /* Also makes the current ProcessInstance available for injection */
  @Produces @Named public ProcessInstance getProcessInstance() {
    assertProcessIdSet();    
    return processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(associationManager.getProcessInstanceId())
      .singleResult();
  }

}
