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
import java.util.Set;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.cmd.*;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilderImpl;
import org.activiti.engine.runtime.DataObject;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.runtime.NativeProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;


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

  public ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, null, tenantId));
  }

  public ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String businessKey, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, businessKey, null, tenantId));
  }

  public ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, Map<String, Object> variables, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, variables, tenantId));
  }

  public ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String businessKey, Map<String, Object> variables, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, businessKey, variables, tenantId));
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

  public Map<String, VariableInstance> getVariableInstances(String executionId) {
    return commandExecutor.execute(new GetExecutionVariableInstancesCmd(executionId, null, false));
  }

  public List<VariableInstance> getVariableInstancesByExecutionIds(Set<String> executionIds) {
    return commandExecutor.execute(new GetExecutionsVariablesCmd(executionIds));
  }

  public Map<String, Object> getVariablesLocal(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, true));
  }

  public Map<String, VariableInstance> getVariableInstancesLocal(String executionId) {
    return commandExecutor.execute(new GetExecutionVariableInstancesCmd(executionId, null, true));
  }

  public Map<String, Object> getVariables(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, false));
  }

  public Map<String, VariableInstance> getVariableInstances(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariableInstancesCmd(executionId, variableNames, false));
  }

  public Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, true));
  }

  public Map<String, VariableInstance> getVariableInstancesLocal(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariableInstancesCmd(executionId, variableNames, true));
  }

  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, false));
  }

  public VariableInstance getVariableInstance(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableInstanceCmd(executionId, variableName, false));
  }

  public <T> T getVariable(String executionId, String variableName, Class<T> variableClass) {
    return variableClass.cast(getVariable(executionId, variableName));
  }

  public boolean hasVariable(String executionId, String variableName) {
    return commandExecutor.execute(new HasExecutionVariableCmd(executionId, variableName, false));
  }

  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, true));
  }

  public VariableInstance getVariableInstanceLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableInstanceCmd(executionId, variableName, true));
  }

  public <T> T getVariableLocal(String executionId, String variableName, Class<T> variableClass) {
    return variableClass.cast(getVariableLocal(executionId, variableName));
  }

  public boolean hasVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new HasExecutionVariableCmd(executionId, variableName, true));
  }

  public void setVariable(String executionId, String variableName, Object value) {
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }

  public void setVariableLocal(String executionId, String variableName, Object value) {
    if (variableName == null) {
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
    Collection<String> variableNames = new ArrayList<String>(1);
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

  @Override
  public Map<String, DataObject> getDataObjects(String executionId) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, null, false));
  }

  @Override
  public Map<String, DataObject> getDataObjects(String executionId, String locale, boolean withLocalizationFallback) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, null, false, locale, withLocalizationFallback));
  }

  @Override
  public Map<String, DataObject> getDataObjectsLocal(String executionId) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, null, true));
  }

  @Override
  public Map<String, DataObject> getDataObjectsLocal(String executionId, String locale, boolean withLocalizationFallback) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, null, true, locale, withLocalizationFallback));
  }

  @Override
  public Map<String, DataObject> getDataObjects(String executionId, Collection<String> dataObjectNames) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, dataObjectNames, false));
  }

  @Override
  public Map<String, DataObject> getDataObjects(String executionId, Collection<String> dataObjectNames, String locale, boolean withLocalizationFallback) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, dataObjectNames, false, locale, withLocalizationFallback));
  }

  @Override
  public Map<String, DataObject> getDataObjectsLocal(String executionId, Collection<String> dataObjects) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, dataObjects, true));
  }

  @Override
  public Map<String, DataObject> getDataObjectsLocal(String executionId, Collection<String> dataObjectNames, String locale, boolean withLocalizationFallback) {
    return commandExecutor.execute(new GetDataObjectsCmd(executionId, dataObjectNames, true, locale, withLocalizationFallback));
  }

  @Override
  public DataObject getDataObject(String executionId, String dataObject) {
    return commandExecutor.execute(new GetDataObjectCmd(executionId, dataObject, false));
  }

  @Override
  public DataObject getDataObject(String executionId, String dataObjectName, String locale, boolean withLocalizationFallback) {
    return commandExecutor.execute(new GetDataObjectCmd(executionId, dataObjectName, false, locale, withLocalizationFallback));
  }

  @Override
  public DataObject getDataObjectLocal(String executionId, String dataObjectName) {
    return commandExecutor.execute(new GetDataObjectCmd(executionId, dataObjectName, true));
  }

  @Override
  public DataObject getDataObjectLocal(String executionId, String dataObjectName, String locale, boolean withLocalizationFallback) {
    return commandExecutor.execute(new GetDataObjectCmd(executionId, dataObjectName, true, locale, withLocalizationFallback));
  }

  public void signal(String executionId) {
    commandExecutor.execute(new TriggerCmd(executionId, null));
  }

  @Override
  public void trigger(String executionId) {
    commandExecutor.execute(new TriggerCmd(executionId, null));
  }

  public void signal(String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new TriggerCmd(executionId, processVariables));
  }

  public void trigger(String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new TriggerCmd(executionId, processVariables));
  }

  public void trigger(String executionId, Map<String, Object> processVariables, Map<String, Object> transientVariables) {
    commandExecutor.execute(new TriggerCmd(executionId, processVariables, transientVariables));
  }

  public void addUserIdentityLink(String processInstanceId, String userId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkForProcessInstanceCmd(processInstanceId, userId, null, identityLinkType));
  }

  public void addGroupIdentityLink(String processInstanceId, String groupId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkForProcessInstanceCmd(processInstanceId, null, groupId, identityLinkType));
  }

  public void addParticipantUser(String processInstanceId, String userId) {
    commandExecutor.execute(new AddIdentityLinkForProcessInstanceCmd(processInstanceId, userId, null, IdentityLinkType.PARTICIPANT));
  }

  public void addParticipantGroup(String processInstanceId, String groupId) {
    commandExecutor.execute(new AddIdentityLinkForProcessInstanceCmd(processInstanceId, null, groupId, IdentityLinkType.PARTICIPANT));
  }

  public void deleteParticipantUser(String processInstanceId, String userId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessInstanceCmd(processInstanceId, userId, null, IdentityLinkType.PARTICIPANT));
  }

  public void deleteParticipantGroup(String processInstanceId, String groupId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessInstanceCmd(processInstanceId, null, groupId, IdentityLinkType.PARTICIPANT));
  }

  public void deleteUserIdentityLink(String processInstanceId, String userId, String identityLinkType) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessInstanceCmd(processInstanceId, userId, null, identityLinkType));
  }

  public void deleteGroupIdentityLink(String processInstanceId, String groupId, String identityLinkType) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessInstanceCmd(processInstanceId, null, groupId, identityLinkType));
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

  public void suspendProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new SuspendProcessInstanceCmd(processInstanceId));
  }

  public void activateProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new ActivateProcessInstanceCmd(processInstanceId));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, null, null));
  }

  public ProcessInstance startProcessInstanceByMessageAndTenantId(String messageName, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, null, tenantId));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, null, null));
  }

  public ProcessInstance startProcessInstanceByMessageAndTenantId(String messageName, String businessKey, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, null, tenantId));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, processVariables, null));
  }

  public ProcessInstance startProcessInstanceByMessageAndTenantId(String messageName, Map<String, Object> processVariables, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, processVariables, tenantId));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, processVariables, null));
  }

  @Override
  public ProcessInstance startProcessInstanceByMessageAndTenantId(String messageName, String businessKey, Map<String, Object> processVariables, String tenantId) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, processVariables, tenantId));
  }

  public void signalEventReceived(String signalName) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, null, null));
  }

  public void signalEventReceivedWithTenantId(String signalName, String tenantId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, null, tenantId));
  }

  public void signalEventReceivedAsync(String signalName) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, true, null));
  }

  public void signalEventReceivedAsyncWithTenantId(String signalName, String tenantId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, true, tenantId));
  }

  public void signalEventReceived(String signalName, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, processVariables, null));
  }

  public void signalEventReceivedWithTenantId(String signalName, Map<String, Object> processVariables, String tenantId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, processVariables, tenantId));
  }

  public void signalEventReceived(String signalName, String executionId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, null, null));
  }

  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, processVariables, null));
  }

  public void signalEventReceivedAsync(String signalName, String executionId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, true, null));
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

  @Override
  public void addEventListener(ActivitiEventListener listenerToAdd) {
    commandExecutor.execute(new AddEventListenerCommand(listenerToAdd));
  }

  @Override
  public void addEventListener(ActivitiEventListener listenerToAdd, ActivitiEventType... types) {
    commandExecutor.execute(new AddEventListenerCommand(listenerToAdd, types));
  }

  @Override
  public void removeEventListener(ActivitiEventListener listenerToRemove) {
    commandExecutor.execute(new RemoveEventListenerCommand(listenerToRemove));
  }

  @Override
  public void dispatchEvent(ActivitiEvent event) {
    commandExecutor.execute(new DispatchEventCommand(event));
  }

  @Override
  public void setProcessInstanceName(String processInstanceId, String name) {
    commandExecutor.execute(new SetProcessInstanceNameCmd(processInstanceId, name));
  }

  @Override
  public List<Event> getProcessInstanceEvents(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceEventsCmd(processInstanceId));
  }

  @Override
  public List<FlowNode> getEnabledActivitiesFromAdhocSubProcess(String executionId) {
    return commandExecutor.execute(new GetEnabledActivitiesForAdhocSubProcessCmd(executionId));
  }

  @Override
  public Execution executeActivityInAdhocSubProcess(String executionId, String activityId) {
    return commandExecutor.execute(new ExecuteActivityForAdhocSubProcessCmd(executionId, activityId));
  }

  @Override
  public void completeAdhocSubProcess(String executionId) {
    commandExecutor.execute(new CompleteAdhocSubProcessCmd(executionId));
  }

  @Override
  public ProcessInstanceBuilder createProcessInstanceBuilder() {
    return new ProcessInstanceBuilderImpl(this);
  }

  public ProcessInstance startProcessInstance(ProcessInstanceBuilderImpl processInstanceBuilder) {
    if (processInstanceBuilder.getProcessDefinitionId() != null || processInstanceBuilder.getProcessDefinitionKey() != null) {
      return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processInstanceBuilder));
    } else if (processInstanceBuilder.getMessageName() != null) {
      return commandExecutor.execute(new StartProcessInstanceByMessageCmd(processInstanceBuilder));
    } else {
      throw new ActivitiIllegalArgumentException("No processDefinitionId, processDefinitionKey nor messageName provided");
    }
  }

  public ProcessInstance createProcessInstance(ProcessInstanceBuilderImpl processInstanceBuilder) {
    if (processInstanceBuilder.getProcessDefinitionId() != null || processInstanceBuilder.getProcessDefinitionKey() != null) {
        return commandExecutor.execute(new CreateProcessInstanceCmd(processInstanceBuilder));
    } else {
        throw new ActivitiIllegalArgumentException("No processDefinitionId, processDefinitionKey nor messageName provided");
    }
  }
}
