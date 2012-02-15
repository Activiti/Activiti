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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Daniel Meyer
 */
public class StartProcessInstanceByMessageCmd implements Command<ProcessInstance> {

  protected final String messageName;
  protected final String businessKey;
  protected final Map<String, Object> processVariables;

  public StartProcessInstanceByMessageCmd(String messageName, String businessKey, Map<String, Object> processVariables) {
    this.messageName = messageName;
    this.businessKey = businessKey;
    this.processVariables = processVariables;    
  }

  public ProcessInstance execute(CommandContext commandContext) {
    
    if(messageName == null) {
      throw new ActivitiException("Cannot start process instance by message: message name is null");
    }
    
    MessageEventSubscriptionEntity messageEventSubscription = commandContext.getEventSubscriptionManager()
      .findMessageStartEventSubscriptionByName(messageName);
    
    if(messageEventSubscription == null) {
      throw new ActivitiException("Cannot start process instance by message: no subscription to message with name '"+messageName+"' found.");
    }
    
    String processDefinitionKey = messageEventSubscription.getConfiguration();
    if(processDefinitionKey == null) {
      throw new ActivitiException("Cannot start process instance by message: subscription to message with name '"+messageName+"' is not a message start event.");
    }
        
    DeploymentCache deploymentCache = Context
            .getProcessEngineConfiguration()
            .getDeploymentCache();
          
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition == null) {
        throw new ActivitiException("No process definition found for key '" + processDefinitionKey + "'");
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
