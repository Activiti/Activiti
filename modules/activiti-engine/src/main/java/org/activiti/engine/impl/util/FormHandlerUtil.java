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
import org.activiti.engine.impl.form.DefaultStartFormHandler;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * @author Joram Barrez
 */
public class FormHandlerUtil {
  
  public static StartFormHandler getStartFormHandler(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity) {
    StartFormHandler startFormHandler = new DefaultStartFormHandler();
    org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionEntity.getId());
    
    FlowElement initialFlowElement = process.getInitialFlowElement();
    if (initialFlowElement instanceof StartEvent) {
      
      StartEvent startEvent = (StartEvent) initialFlowElement;
      
      List<FormProperty> formProperties = startEvent.getFormProperties();
      String formKey = startEvent.getFormKey();
      DeploymentEntity deploymentEntity = commandContext.getDeploymentEntityManager().findDeploymentById(processDefinitionEntity.getDeploymentId());
      
      startFormHandler.parseConfiguration(formProperties, formKey, deploymentEntity, processDefinitionEntity);
      return startFormHandler;
    }
    
    return null;
    
  }

}
