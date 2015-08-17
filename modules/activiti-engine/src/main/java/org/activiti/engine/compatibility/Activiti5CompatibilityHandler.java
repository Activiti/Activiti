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
package org.activiti.engine.compatibility;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public interface Activiti5CompatibilityHandler {

  public static final String ACTIVITI_5_ENGINE_TAG = "activiti-5";
  
  ProcessDefinition getProcessDefinition(String processDefinitionId);
  
  ProcessDefinition getProcessDefinitionByKey(String processDefinitionKey);
  
  void suspendProcessDefinition(String processDefinitionId, String processDefinitionKey, boolean suspendProcessInstances, Date suspensionDate, String tenantId);

  void activateProcessDefinition(String processDefinitionId, String processDefinitionKey, boolean activateProcessInstances, Date activationDate, String tenantId);
  
  Deployment deploy(DeploymentBuilderImpl deploymentBuilder);
  
  void setDeploymentCategory(String deploymentId, String category);
  
  void deleteDeployment(String deploymentId, boolean cascade);
  
  ProcessInstance startProcessInstance(String processDefinitionKey, String processDefinitionId, Map<String, Object> variables, String businessKey, String tenantId, String processInstanceName);
  
  void suspendProcessInstance(String processInstanceId);
  
  void activateProcessInstance(String processInstanceId);
  
  void deleteProcessInstance(String processInstanceId, String deleteReason);
  
  void completeTask(TaskEntity taskEntity, Map<String, Object> variables, boolean localScope);
  
  ProcessInstance submitStartFormData(String processDefinitionId, String businessKey, Map<String, String> properties);
  
  void submitTaskFormData(String taskId, Map<String, String> properties, boolean completeTask);
  
  void saveTask(TaskEntity task);
  
  void addIdentityLink(String taskId, String identityId, int identityIdType, String identityType);
  
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content, String url);
  
  void trigger(String executionId, Map<String, Object> processVariables);
  
  void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables, boolean async);
  
  void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables);
  
  void signalEventReceived(SignalEventSubscriptionEntity signalEventSubscriptionEntity, Object payload, boolean async);

  void executeJob(Job job);
  
  void executeJobWithLockAndRetry(JobEntity job);
  
  void addEventListener(Object listener);
  
  void removeEventListener(Object listener);
  
  void setClock(Clock clock);
  
  Object getRawProcessConfiguration();
  
  Object getRawCommandExecutor();
}
