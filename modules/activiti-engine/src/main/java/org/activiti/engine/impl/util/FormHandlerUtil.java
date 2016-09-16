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
package org.activiti.engine.impl.util;

import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.form.DefaultStartFormHandler;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Joram Barrez
 */
public class FormHandlerUtil {
  
  public static StartFormHandler getStartFormHandler(CommandContext commandContext, ProcessDefinition processDefinition) {
    StartFormHandler startFormHandler = new DefaultStartFormHandler();
    org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinition.getId());
    
    FlowElement initialFlowElement = process.getInitialFlowElement();
    if (initialFlowElement instanceof StartEvent) {
      
      StartEvent startEvent = (StartEvent) initialFlowElement;
      
      List<FormProperty> formProperties = startEvent.getFormProperties();
      String formKey = startEvent.getFormKey();
      DeploymentEntity deploymentEntity = commandContext.getDeploymentEntityManager().findById(processDefinition.getDeploymentId());
      
      startFormHandler.parseConfiguration(formProperties, formKey, deploymentEntity, processDefinition);
      return startFormHandler;
    }
    
    return null;
    
  }
  
  public static TaskFormHandler getTaskFormHandlder(String processDefinitionId, String taskId) {
    org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
    FlowElement flowElement = process.getFlowElement(taskId, true);
    if (flowElement instanceof UserTask) {
      UserTask userTask = (UserTask) flowElement;
      
      ProcessDefinition processDefinitionEntity = ProcessDefinitionUtil.getProcessDefinition(processDefinitionId);
      DeploymentEntity deploymentEntity = Context.getProcessEngineConfiguration()
          .getDeploymentEntityManager().findById(processDefinitionEntity.getDeploymentId());
      
      TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();
      taskFormHandler.parseConfiguration(userTask.getFormProperties(), userTask.getFormKey(), deploymentEntity, processDefinitionEntity);
      
      return taskFormHandler;
    }
    
    return null;
  }
  
  public static TaskFormHandler getTaskFormHandlder(TaskEntity taskEntity) {
    if (taskEntity.getProcessDefinitionId() != null) {
      return getTaskFormHandlder(taskEntity.getProcessDefinitionId(), taskEntity.getTaskDefinitionKey());
    }
    return null;
  }
  

}
