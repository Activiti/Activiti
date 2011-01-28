package org.activiti.engine.impl.webservice.experimental;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

/**
 * Delegation class to provide the Service as WebService. The Maps are currently a problem, since
 * JAX-B cannot handle java.util.Map as parameter. 
 * @author ruecker
 */
@WebService
public class RuntimeWebServiceImpl {

  private final ProcessEngine processEngine;

  public RuntimeWebServiceImpl(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @WebMethod
  public void setVariable(String executionId, String variableName, Object value) {
    processEngine.getRuntimeService().setVariable(executionId, variableName, value);
  }

  @WebMethod
  public void setVariables(String executionId, HashMap<String, ? extends Object> variables) {
  }

  @WebMethod
  public HashMap<String, Object> getVariables(String executionId) {
    return (HashMap<String, Object>) processEngine.getRuntimeService().getVariables(executionId);
  }

  @WebMethod
  public String startProcessInstanceByKey(String processDefinitionKey, HashMap<String, Object> variables) {
    return null;
  }

  
  //// 
  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return null;
  }

  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
    return null;
  }


  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
    return null;
  }

  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return null;
  }

  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
    return null;
  }

  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return null;
  }

  @WebMethod(exclude=true)
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    return null;
  }

  @WebMethod(exclude=true)
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
  }

  @WebMethod(exclude=true)
  public List<String> getActiveActivityIds(String executionId) {
    return null;
  }

  @WebMethod(exclude=true)
  public void signal(String executionId) {
  }

  @WebMethod(exclude=true)
  public Map<String, Object> getVariablesLocal(String executionId) {
    return null;
  }

  @WebMethod(exclude=true)
  public Map<String, Object> getVariables(String executionId, Collection<String> variableNames) {
    return null;
  }

  @WebMethod(exclude=true)
  public Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames) {
    return null;
  }

  @WebMethod(exclude=true)
  public Object getVariable(String executionId, String variableName) {
    return null;
  }

  @WebMethod(exclude=true)
  public Object getVariableLocal(String executionId, String variableName) {
    return null;
  }

  @WebMethod(exclude=true)
  public void setVariableLocal(String executionId, String variableName, Object value) {
  }

  @WebMethod(exclude=true)
  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
  }

  @WebMethod(exclude=true)
  public ExecutionQuery createExecutionQuery() {
    return null;
  }

  @WebMethod(exclude=true)
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return null;
  }



}
