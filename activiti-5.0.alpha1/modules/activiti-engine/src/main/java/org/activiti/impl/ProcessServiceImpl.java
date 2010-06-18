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
import java.util.List;
import java.util.Map;

import org.activiti.Deployment;
import org.activiti.DeploymentBuilder;
import org.activiti.ProcessDefinition;
import org.activiti.ProcessInstance;
import org.activiti.ProcessInstanceQuery;
import org.activiti.ProcessService;
import org.activiti.impl.cmd.DeleteDeploymentCmd;
import org.activiti.impl.cmd.DeployCmd;
import org.activiti.impl.cmd.FindDeploymentResourcesCmd;
import org.activiti.impl.cmd.FindDeploymentsCmd;
import org.activiti.impl.cmd.FindProcessDefinitionsCmd;
import org.activiti.impl.cmd.FindProcessInstanceCmd;
import org.activiti.impl.cmd.GetDeploymentResourceCmd;
import org.activiti.impl.cmd.StartProcessInstanceCmd;
import org.activiti.impl.execution.ProcessInstanceQueryImpl;
import org.activiti.impl.repository.DeploymentBuilderImpl;
import org.activiti.impl.repository.DeploymentImpl;


/**
 * @author Tom Baeyens
 */
public class ProcessServiceImpl extends ServiceImpl implements ProcessService {
  
  public ProcessServiceImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }

  public void close() {
  }

  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public DeploymentBuilder newDeployment() {
    return new DeploymentBuilderImpl(this);
  }

  public ProcessInstance findProcessInstanceById(String id) {
    return cmdExecutor.execute(new FindProcessInstanceCmd(id), processEngine);
  }

  public void deleteDeployment(String deploymentId) {
    cmdExecutor.execute(new DeleteDeploymentCmd(deploymentId, false), processEngine);
  }
  
  public void deleteDeploymentCascade(String deploymentId) {
    cmdExecutor.execute(new DeleteDeploymentCmd(deploymentId, true), processEngine);
  }

  public void deleteProcessInstance(String processInstance) {
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return cmdExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null), processEngine);
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return cmdExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, variables), processEngine);
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return cmdExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null), processEngine);
  }
  
  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return cmdExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, variables), processEngine);
  }

  public Deployment deploy(DeploymentImpl deployment) {
    return cmdExecutor.execute(new DeployCmd<Deployment>(deployment), processEngine);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitions() {
    return cmdExecutor.execute(new FindProcessDefinitionsCmd(), processEngine);
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeployments() {
    return cmdExecutor.execute(new FindDeploymentsCmd(), processEngine);
  }
  
  @SuppressWarnings("unchecked")
  public List<String> findDeploymentResources(String deploymentId) {
    return cmdExecutor.execute(new FindDeploymentResourcesCmd(deploymentId), processEngine);
  }
  
  public InputStream getDeploymentResourceContent(String deploymentId, String resourceName) {
    return cmdExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName), processEngine);
  }
  
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(processEngine);
  }
}
