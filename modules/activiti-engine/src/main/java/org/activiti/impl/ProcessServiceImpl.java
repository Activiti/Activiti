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
package org.activiti.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.Deployment;
import org.activiti.DeploymentBuilder;
import org.activiti.Execution;
import org.activiti.ProcessDefinition;
import org.activiti.ProcessInstance;
import org.activiti.ProcessInstanceQuery;
import org.activiti.ProcessService;
import org.activiti.impl.cmd.DeleteDeploymentCmd;
import org.activiti.impl.cmd.DeleteProcessInstanceCmd;
import org.activiti.impl.cmd.DeployCmd;
import org.activiti.impl.cmd.FindDeploymentResourcesCmd;
import org.activiti.impl.cmd.FindDeploymentsCmd;
import org.activiti.impl.cmd.FindExecutionCmd;
import org.activiti.impl.cmd.FindExecutionInActivityCmd;
import org.activiti.impl.cmd.FindProcessDefinitionCmd;
import org.activiti.impl.cmd.FindProcessDefinitionsCmd;
import org.activiti.impl.cmd.FindProcessInstanceCmd;
import org.activiti.impl.cmd.GetDeploymentResourceCmd;
import org.activiti.impl.cmd.GetExecutionVariableCmd;
import org.activiti.impl.cmd.GetExecutionVariablesCmd;
import org.activiti.impl.cmd.GetFormCmd;
import org.activiti.impl.cmd.SendEventCmd;
import org.activiti.impl.cmd.SetExecutionVariablesCmd;
import org.activiti.impl.cmd.StartProcessInstanceCmd;
import org.activiti.impl.execution.ProcessInstanceQueryImpl;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.repository.DeploymentBuilderImpl;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.scripting.ScriptingEngines;


/**
 * @author Tom Baeyens
 */
public class ProcessServiceImpl implements ProcessService {
  
  private final CommandExecutor commandExecutor;
  
  private final DeployerManager deployerManager;

  private final ScriptingEngines scriptingEngines;

  public ProcessServiceImpl(CommandExecutor commandExecutor, DeployerManager deployerManager, ScriptingEngines scriptingEngines) {
    this.commandExecutor = commandExecutor;
    this.deployerManager = deployerManager;
    this.scriptingEngines = scriptingEngines;
  }

  public DeploymentBuilder createDeployment() {
    return new DeploymentBuilderImpl(this);
  }

  public ProcessInstance findProcessInstanceById(String id) {
    return commandExecutor.execute(new FindProcessInstanceCmd(id));
  }
  
  public Execution findExecutionById(String id) {
    return commandExecutor.execute(new FindExecutionCmd(id));
  }
  
  public void deleteDeployment(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, false));
  }
  
  public void deleteDeploymentCascade(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, true));
  }

  public void deleteProcessInstance(String processInstanceId) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, variables));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null));
  }
  
  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, variables));
  }

  public Deployment deploy(DeploymentImpl deployment) {
    return commandExecutor.execute(new DeployCmd<Deployment>(deployerManager, deployment));
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitions() {
    return commandExecutor.execute(new FindProcessDefinitionsCmd());
  }
  
  public ProcessDefinition findProcessDefinitionById(String processDefinitionId) {
    return commandExecutor.execute(new FindProcessDefinitionCmd(processDefinitionId));
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeployments() {
    return commandExecutor.execute(new FindDeploymentsCmd());
  }
  
  @SuppressWarnings("unchecked")
  public List<String> findDeploymentResources(String deploymentId) {
    return commandExecutor.execute(new FindDeploymentResourcesCmd(deploymentId));
  }
  
  public InputStream getDeploymentResourceContent(String deploymentId, String resourceName) {
    return commandExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName));
  }
  
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(commandExecutor);
  }

  public Map<String, Object> getVariables(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId));
  }
  
  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName));
  }

  public void setVariable(String executionId, String variableName, Object value) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables));
  }

  public void setVariables(String executionId, Map<String, Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables));
  }

  public Object getStartFormById(String processDefinitionId) {
    return commandExecutor.execute(new GetFormCmd(scriptingEngines, processDefinitionId, null, null));
  }

  public Object getStartFormByKey(String processDefinitionKey) {
    return commandExecutor.execute(new GetFormCmd(scriptingEngines, null, processDefinitionKey, null));
  }

  public void sendEvent(String executionId) {
    commandExecutor.execute(new SendEventCmd(executionId, null));
  }

  public void sendEvent(String executionId, Object eventData) {
    commandExecutor.execute(new SendEventCmd(executionId, eventData));
  }
  
  public Execution findExecutionInActivity(String processInstanceId, String activityId) {
    return commandExecutor.execute(new FindExecutionInActivityCmd(processInstanceId, activityId));
  }
}
