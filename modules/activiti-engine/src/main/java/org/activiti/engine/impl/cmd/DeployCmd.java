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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeployCmd<T> implements Command<Deployment>, Serializable {

  private static final long serialVersionUID = 1L;
  protected DeploymentBuilderImpl deploymentBuilder;

  public DeployCmd(DeploymentBuilderImpl deploymentBuilder) {
    this.deploymentBuilder = deploymentBuilder;
  }

  public Deployment execute(CommandContext commandContext) {
    DeploymentEntity deployment = deploymentBuilder.getDeployment();

    deployment.setDeploymentTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());

    if ( deploymentBuilder.isDuplicateFilterEnabled() ) {
    	
    	List<Deployment> existingDeployments = new ArrayList<Deployment>();
      if (deployment.getTenantId() == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(deployment.getTenantId())) {
      	DeploymentEntity existingDeployment = commandContext
     			 .getDeploymentEntityManager()
     			 .findLatestDeploymentByName(deployment.getName());
      	if (existingDeployment != null) {
      		existingDeployments.add(existingDeployment);
      	}
      } else {
      	 List<Deployment> deploymentList = commandContext
             .getProcessEngineConfiguration().getRepositoryService()
             .createDeploymentQuery()
             .deploymentName(deployment.getName())
             .deploymentTenantId(deployment.getTenantId())
             .orderByDeploymentId().desc().list();
      	 
      	 if (!deploymentList.isEmpty()) {
      		 existingDeployments.addAll(deploymentList);
      	 }
      }
      		
      DeploymentEntity existingDeployment = null;
      if(!existingDeployments.isEmpty()) {
        existingDeployment = (DeploymentEntity) existingDeployments.get(0);
      }
      
      if ( (existingDeployment!=null)
           && !deploymentsDiffer(deployment, existingDeployment)) {
        return existingDeployment;
      }
    }

    deployment.setNew(true);
    
    // Save the data
    commandContext
      .getDeploymentEntityManager()
      .insertDeployment(deployment);
    
    if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
	    commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
	    		ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, deployment));
    }
    
    // Deployment settings
    Map<String, Object> deploymentSettings = new HashMap<String, Object>();
    deploymentSettings.put(DeploymentSettings.IS_BPMN20_XSD_VALIDATION_ENABLED, deploymentBuilder.isBpmn20XsdValidationEnabled());
    deploymentSettings.put(DeploymentSettings.IS_PROCESS_VALIDATION_ENABLED, deploymentBuilder.isProcessValidationEnabled());
    
    // Actually deploy
    commandContext
      .getProcessEngineConfiguration()
      .getDeploymentManager()
      .deploy(deployment, deploymentSettings);
    
    if (deploymentBuilder.getProcessDefinitionsActivationDate() != null) {
      scheduleProcessDefinitionActivation(commandContext, deployment);
    }
    
    if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
	    commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
	    		ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, deployment));
    }
    
    return deployment;
  }

  protected boolean deploymentsDiffer(DeploymentEntity deployment, DeploymentEntity saved) {
    
    if(deployment.getResources() == null || saved.getResources() == null) {
      return true;
    }
    
    Map<String, ResourceEntity> resources = deployment.getResources();
    Map<String, ResourceEntity> savedResources = saved.getResources();
    
    for (String resourceName: resources.keySet()) {
      ResourceEntity savedResource = savedResources.get(resourceName);
      
      if(savedResource == null) return true;
      
      if(!savedResource.isGenerated()) {
        ResourceEntity resource = resources.get(resourceName);
        
        byte[] bytes = resource.getBytes();
        byte[] savedBytes = savedResource.getBytes();
        if (!Arrays.equals(bytes, savedBytes)) {
          return true;
        }
      }
    }
    return false;
  }
  
  protected void scheduleProcessDefinitionActivation(CommandContext commandContext, DeploymentEntity deployment) {
    for (ProcessDefinitionEntity processDefinitionEntity : deployment.getDeployedArtifacts(ProcessDefinitionEntity.class)) {
      
      // If activation date is set, we first suspend all the process definition
      SuspendProcessDefinitionCmd suspendProcessDefinitionCmd = 
              new SuspendProcessDefinitionCmd(processDefinitionEntity, false, null, deployment.getTenantId());
      suspendProcessDefinitionCmd.execute(commandContext);
      
      // And we schedule an activation at the provided date
      ActivateProcessDefinitionCmd activateProcessDefinitionCmd =new ActivateProcessDefinitionCmd(
      		processDefinitionEntity, false, deploymentBuilder.getProcessDefinitionsActivationDate(), deployment.getTenantId());
      activateProcessDefinitionCmd.execute(commandContext);
    }
  }

//  private boolean resourcesDiffer(ByteArrayEntity value, ByteArrayEntity other) {
//    if (value == null && other == null) {
//      return false;
//    }
//    String bytes = createKey(value.getBytes());
//    String savedBytes = other == null ? null : createKey(other.getBytes());
//    return !bytes.equals(savedBytes);
//  }
//
//  private String createKey(byte[] bytes) {
//    if (bytes == null) {
//      return "";
//    }
//    MessageDigest digest;
//    try {
//      digest = MessageDigest.getInstance("MD5");
//    } catch (NoSuchAlgorithmException e) {
//      throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
//    }
//    bytes = digest.digest(bytes);
//    return String.format("%032x", new BigInteger(1, bytes));
//  }
}
