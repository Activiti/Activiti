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

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cmd.ActivateProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.AddEditorSourceExtraForModelCmd;
import org.activiti.engine.impl.cmd.AddEditorSourceForModelCmd;
import org.activiti.engine.impl.cmd.AddIdentityLinkForProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.impl.cmd.CreateModelCmd;
import org.activiti.engine.impl.cmd.DeleteDeploymentCmd;
import org.activiti.engine.impl.cmd.DeleteIdentityLinkForProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.DeleteModelCmd;
import org.activiti.engine.impl.cmd.DeployCmd;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDiagramCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDiagramLayoutCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessModelCmd;
import org.activiti.engine.impl.cmd.GetDeploymentResourceCmd;
import org.activiti.engine.impl.cmd.GetDeploymentResourceNamesCmd;
import org.activiti.engine.impl.cmd.GetIdentityLinksForProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.GetModelCmd;
import org.activiti.engine.impl.cmd.GetModelEditorSourceCmd;
import org.activiti.engine.impl.cmd.GetModelEditorSourceExtraCmd;
import org.activiti.engine.impl.cmd.IsProcessDefinitionSuspendedCmd;
import org.activiti.engine.impl.cmd.SaveModelCmd;
import org.activiti.engine.impl.cmd.SetDeploymentCategoryCmd;
import org.activiti.engine.impl.cmd.SetProcessDefinitionCategoryCmd;
import org.activiti.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.ValidateBpmnModelCmd;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.DiagramLayout;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.NativeDeploymentQuery;
import org.activiti.engine.repository.NativeModelQuery;
import org.activiti.engine.repository.NativeProcessDefinitionQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.validation.ValidationError;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public class RepositoryServiceImpl extends ServiceImpl implements RepositoryService {

  public DeploymentBuilder createDeployment() {
    return new DeploymentBuilderImpl(this);
  }

  public Deployment deploy(DeploymentBuilderImpl deploymentBuilder) {
    return commandExecutor.execute(new DeployCmd<Deployment>(deploymentBuilder));
  }

  public void deleteDeployment(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, false));
  }

  public void deleteDeploymentCascade(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, true));
  }
  
  public void deleteDeployment(String deploymentId, boolean cascade) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, cascade));
  }
  
  public void setDeploymentCategory(String deploymentId, String category) {
    commandExecutor.execute(new SetDeploymentCategoryCmd(deploymentId, category));
  }

  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl(commandExecutor);
  }

  @Override
  public NativeProcessDefinitionQuery createNativeProcessDefinitionQuery() {
    return new NativeProcessDefinitionQueryImpl(commandExecutor);
  }

  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return commandExecutor.execute(new GetDeploymentResourceNamesCmd(deploymentId));
  }

  public InputStream getResourceAsStream(String deploymentId, String resourceName) {
    return commandExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName));
  }
  
  @Override
  public void changeDeploymentTenantId(String deploymentId, String newTenantId) {
  	commandExecutor.execute(new ChangeDeploymentTenantIdCmd(deploymentId, newTenantId));
  }

  public DeploymentQuery createDeploymentQuery() {
    return new DeploymentQueryImpl(commandExecutor);
  }

  @Override
  public NativeDeploymentQuery createNativeDeploymentQuery() {
    return new NativeDeploymentQueryImpl(commandExecutor);
  }

  public ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDefinitionCmd(processDefinitionId));
  }
  
  public BpmnModel getBpmnModel(String processDefinitionId) {
    return commandExecutor.execute(new GetBpmnModelCmd(processDefinitionId));
  }
  
  public ReadOnlyProcessDefinition getDeployedProcessDefinition(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDefinitionCmd(processDefinitionId));
  }
  
  public boolean isProcessDefinitionSuspended(String processDefinitionId) {
    return commandExecutor.execute(new IsProcessDefinitionSuspendedCmd(processDefinitionId));
  }

  public void suspendProcessDefinitionById(String processDefinitionId) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(processDefinitionId, null, false, null, null));
  }
  
  public void suspendProcessDefinitionById(String processDefinitionId, boolean suspendProcessInstances, Date suspensionDate) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(processDefinitionId, null, suspendProcessInstances, suspensionDate, null));
  }
  
  public void suspendProcessDefinitionByKey(String processDefinitionKey) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, false, null, null));
  }

  public void suspendProcessDefinitionByKey(String processDefinitionKey, boolean suspendProcessInstances, Date suspensionDate) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, suspendProcessInstances, suspensionDate, null));
  }
  
  public void suspendProcessDefinitionByKey(String processDefinitionKey, String tenantId) {
  	commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, false, null, tenantId));
  }
  
  public void suspendProcessDefinitionByKey(String processDefinitionKey,
      boolean suspendProcessInstances, Date suspensionDate, String tenantId) {
  	commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, suspendProcessInstances, suspensionDate, tenantId));
  }
  
  public void activateProcessDefinitionById(String processDefinitionId) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(processDefinitionId, null, false, null, null));
  }
  
  public void activateProcessDefinitionById(String processDefinitionId, boolean activateProcessInstances, Date activationDate) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(processDefinitionId, null, activateProcessInstances, activationDate, null));
  }

  public void activateProcessDefinitionByKey(String processDefinitionKey) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, false, null, null));
  }
  
  public void activateProcessDefinitionByKey(String processDefinitionKey, boolean activateProcessInstances, Date activationDate) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, activateProcessInstances, activationDate, null));
  }
  
  public void activateProcessDefinitionByKey(String processDefinitionKey, String tenantId) {
  	commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, false, null, tenantId));
  }
  
  public void activateProcessDefinitionByKey(String processDefinitionKey, boolean activateProcessInstances, Date activationDate, String tenantId) {
  	commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, activateProcessInstances, activationDate, tenantId));
  }
  
  public void setProcessDefinitionCategory(String processDefinitionId, String category) {
    commandExecutor.execute(new SetProcessDefinitionCategoryCmd(processDefinitionId, category)); 
  }

  public InputStream getProcessModel(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessModelCmd(processDefinitionId));
  }

  public InputStream getProcessDiagram(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramCmd(processDefinitionId));
  }

  public DiagramLayout getProcessDiagramLayout(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramLayoutCmd(processDefinitionId));
  }
  
  public Model newModel() {
    return commandExecutor.execute(new CreateModelCmd());
  }

  public void saveModel(Model model) {
    commandExecutor.execute(new SaveModelCmd((ModelEntity) model));
  }

  public void deleteModel(String modelId) {
    commandExecutor.execute(new DeleteModelCmd(modelId));
  }
  
  public void addModelEditorSource(String modelId, byte[] bytes) {
    commandExecutor.execute(new AddEditorSourceForModelCmd(modelId, bytes));
  }
  
  public void addModelEditorSourceExtra(String modelId, byte[] bytes) {
    commandExecutor.execute(new AddEditorSourceExtraForModelCmd(modelId, bytes));
  }
  
  public ModelQuery createModelQuery() {
    return new ModelQueryImpl(commandExecutor);
  }

  @Override
  public NativeModelQuery createNativeModelQuery() {
    return new NativeModelQueryImpl(commandExecutor);
  }

  public Model getModel(String modelId) {
    return commandExecutor.execute(new GetModelCmd(modelId));
  }
  
  public byte[] getModelEditorSource(String modelId) {
    return commandExecutor.execute(new GetModelEditorSourceCmd(modelId));
  }
  
  public byte[] getModelEditorSourceExtra(String modelId) {
    return commandExecutor.execute(new GetModelEditorSourceExtraCmd(modelId));
  }
  
  public void addCandidateStarterUser(String processDefinitionId, String userId) {
    commandExecutor.execute(new AddIdentityLinkForProcessDefinitionCmd(processDefinitionId, userId, null));
  }
  
  public void addCandidateStarterGroup(String processDefinitionId, String groupId) {
    commandExecutor.execute(new AddIdentityLinkForProcessDefinitionCmd(processDefinitionId, null, groupId));
  }
  
  public void deleteCandidateStarterGroup(String processDefinitionId, String groupId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessDefinitionCmd(processDefinitionId, null, groupId));
  }

  public void deleteCandidateStarterUser(String processDefinitionId, String userId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessDefinitionCmd(processDefinitionId, userId, null));
  }

  public List<IdentityLink> getIdentityLinksForProcessDefinition(String processDefinitionId) {
    return commandExecutor.execute(new GetIdentityLinksForProcessDefinitionCmd(processDefinitionId));
  }
  
  public List<ValidationError> validateProcess(BpmnModel bpmnModel) {
  	return commandExecutor.execute(new ValidateBpmnModelCmd(bpmnModel));
  }

}
