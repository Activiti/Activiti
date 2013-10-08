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
package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.cmd.ActivateProcessInstanceCmd;
import org.activiti.engine.impl.cmd.AddIdentityLinkForProcessInstanceCmd;
import org.activiti.engine.impl.cmd.DeleteProcessInstanceCmd;
import org.activiti.engine.impl.cmd.FindActiveActivityIdsCmd;
import org.activiti.engine.impl.cmd.GetExecutionVariableCmd;
import org.activiti.engine.impl.cmd.GetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.GetIdentityLinksForProcessInstanceCmd;
import org.activiti.engine.impl.cmd.GetStartFormCmd;
import org.activiti.engine.impl.cmd.HasExecutionVariableCmd;
import org.activiti.engine.impl.cmd.MessageEventReceivedCmd;
import org.activiti.engine.impl.cmd.RemoveExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.SetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.SetProcessInstanceBusinessKeyCmd;
import org.activiti.engine.impl.cmd.SignalCmd;
import org.activiti.engine.impl.cmd.SignalEventReceivedCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceByMessageCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.cmd.SuspendProcessInstanceCmd;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.runtime.NativeProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class RuntimeServiceImpl extends ServiceImpl implements RuntimeService {

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, null));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, businessKey, null));
  }
  
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, variables));
  }
  
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, businessKey, variables));
  }
  
  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null, null));
  }
  
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, businessKey, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null, variables));
  }
  
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, businessKey, variables));
  }
  
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId, deleteReason));
  }

  public ExecutionQuery createExecutionQuery() {
    return new ExecutionQueryImpl(commandExecutor);
  }
  
  public NativeExecutionQuery createNativeExecutionQuery() {
    return new NativeExecutionQueryImpl(commandExecutor);
  }

  public NativeProcessInstanceQuery createNativeProcessInstanceQuery() {
    return new NativeProcessInstanceQueryImpl(commandExecutor);
  } 
  
  public void updateBusinessKey(String processInstanceId, String businessKey) {
    commandExecutor.execute(new SetProcessInstanceBusinessKeyCmd(processInstanceId, businessKey));
  }
  
  public Map<String, Object> getVariables(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, false));
  }

  public Map<String, Object> getVariablesLocal(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, true));
  }

  public Map<String, Object> getVariables(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, false));
  }

  public Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, true));
  }

  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, false));
  }
  
  @Override
  public boolean hasVariable(String executionId, String variableName) {
    return commandExecutor.execute(new HasExecutionVariableCmd(executionId, variableName, false));
  }
  
  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, true));
  }
  
  @Override
  public boolean hasVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new HasExecutionVariableCmd(executionId, variableName, true));
  }
  
  public void setVariable(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }
  
  public void setVariableLocal(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, true));
  }

  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }

  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, true));
  }
  
  public void removeVariable(String executionId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, false));
  }
  
  public void removeVariableLocal(String executionId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, true));
  }

  public void removeVariables(String executionId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, false));    
  }

  public void removeVariablesLocal(String executionId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, true));    
  }

  public void signal(String executionId) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, null));
  }
  
  public void signal(String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, processVariables));
  }

  public void addUserIdentityLink(String processInstanceId, String userId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkForProcessInstanceCmd(processInstanceId, userId, identityLinkType));
  }

  public List<IdentityLink> getIdentityLinksForProcessInstance(String processInstanceId) {
    return commandExecutor.execute(new GetIdentityLinksForProcessInstanceCmd(processInstanceId));
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(commandExecutor);
  }

  public List<String> getActiveActivityIds(String executionId) {
    return commandExecutor.execute(new FindActiveActivityIdsCmd(executionId));
  }

  public FormData getFormInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  public void suspendProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new SuspendProcessInstanceCmd(processInstanceId));
  }

  public void activateProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new ActivateProcessInstanceCmd(processInstanceId));
  }
  
  public ProcessInstance startProcessInstanceByMessage(String messageName) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName,null, null));
  }
  
  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, null));
  }
  
  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, processVariables));
  }
  
  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, processVariables));
  }

  public void signalEventReceived(String signalName) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, null));
  }

  public void signalEventReceivedAsync(String signalName) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, true));
  }
  
  public void signalEventReceived(String signalName, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, processVariables));
  }

  public void signalEventReceived(String signalName, String executionId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, null));
  }

  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, processVariables));
  }

  public void signalEventReceivedAsync(String signalName, String executionId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, true));
  }
  
  public void messageEventReceived(String messageName, String executionId) {
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, null));
  }

  public void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, processVariables));
  }
  
  public void messageEventReceivedAsync(String messageName, String executionId) {
	  commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, true));
  }
}
