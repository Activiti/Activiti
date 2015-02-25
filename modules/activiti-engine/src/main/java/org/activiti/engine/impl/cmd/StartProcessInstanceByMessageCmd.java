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

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author Vasile Dirla
 */
public class StartProcessInstanceByMessageCmd implements Command<ProcessInstance> {

  protected final String messageName;
  protected final String businessKey;
  protected final Map<String, Object> processVariables;
  protected final String tenantId;

  public StartProcessInstanceByMessageCmd(String messageName, String businessKey, Map<String, Object> processVariables, String tenantId) {
    this.messageName = messageName;
    this.businessKey = businessKey;
    this.processVariables = processVariables;
    this.tenantId = tenantId;
  }

  public ProcessInstance execute(CommandContext commandContext) {
    
    if (messageName == null) {
      throw new ActivitiIllegalArgumentException("Cannot start process instance by message: message name is null");
    }
    
    MessageEventSubscriptionEntity messageEventSubscription = commandContext.getEventSubscriptionEntityManager()
          .findMessageStartEventSubscriptionByName(messageName, tenantId);
    
    if (messageEventSubscription == null) {
      throw new ActivitiObjectNotFoundException("Cannot start process instance by message: no subscription to message with name '"+messageName+"' found.", MessageEventSubscriptionEntity.class);
    }
    
    String processDefinitionId = messageEventSubscription.getConfiguration();
    if (processDefinitionId == null) {
      throw new ActivitiException("Cannot start process instance by message: subscription to message with name '"+messageName+"' is not a message start event.");
    }
        
    DeploymentManager deploymentCache = commandContext
            .getProcessEngineConfiguration()
            .getDeploymentManager();
          
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("No process definition found for id '" + processDefinitionId + "'", ProcessDefinition.class);
    }

    // Do not start process a process instance if the process definition is suspended
    if (processDefinition.isSuspended()) {
      throw new ActivitiException("Cannot start process instance. Process definition "
          + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }
    
    ActivityImpl startActivity = processDefinition.findActivity(messageEventSubscription.getActivityId());
    ExecutionEntity processInstance = processDefinition.createProcessInstance(businessKey, startActivity);

    if (processVariables != null) {
      processInstance.setVariables(processVariables);
    }
    
    processInstance.start();
    
    return processInstance;
  }

}
