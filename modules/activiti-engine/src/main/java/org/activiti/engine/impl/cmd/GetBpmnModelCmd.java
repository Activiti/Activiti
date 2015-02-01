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

import java.io.InputStream;
import java.io.Serializable;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.util.io.BytesStreamSource;
import org.activiti.engine.repository.Deployment;

/**
 * @author Joram Barrez
 */
public class GetBpmnModelCmd implements Command<BpmnModel>, Serializable {

  private static final long serialVersionUID = 8167762371289445046L;

  protected String processDefinitionId;

  public GetBpmnModelCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public BpmnModel execute(CommandContext commandContext) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("processDefinitionId is null");
    }

    // Find the bpmn 2.0 xml resource name which is stored on the process definition
    ProcessDefinitionEntity processDefinitionEntity = commandContext
            .getProcessDefinitionEntityManager()
            .findProcessDefinitionById(processDefinitionId);
    
    if (processDefinitionEntity == null) {
      throw new ActivitiObjectNotFoundException("Process definition does not exist: " + processDefinitionId, ProcessDefinitionEntity.class);
    }

    // Fetch the resource
    String resourceName = processDefinitionEntity.getResourceName();
    ResourceEntity resource = commandContext.getResourceEntityManager()
            .findResourceByDeploymentIdAndResourceName(processDefinitionEntity.getDeploymentId(), resourceName);
    if (resource == null) {
      if (commandContext.getDeploymentEntityManager().findDeploymentById(processDefinitionEntity.getDeploymentId()) == null) {
        throw new ActivitiObjectNotFoundException("deployment for process definition does not exist: " 
      + processDefinitionEntity.getDeploymentId(), Deployment.class);
      } else {
        throw new ActivitiObjectNotFoundException("no resource found with name '" + resourceName 
                + "' in deployment '" + processDefinitionEntity.getDeploymentId() + "'", InputStream.class);
      }
    }
    
    // Convert the bpmn 2.0 xml to a bpmn model
    BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    return bpmnXMLConverter.convertToBpmnModel(new BytesStreamSource(resource.getBytes()), false, false); // no need to validate schema, it was already validated on deploy
  }

}
