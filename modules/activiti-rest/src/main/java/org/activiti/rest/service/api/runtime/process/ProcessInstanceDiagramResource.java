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

package org.activiti.rest.service.api.runtime.process;

import java.io.InputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class ProcessInstanceDiagramResource extends BaseProcessInstanceResource {
  
  @Get
  public InputRepresentation getProcessInstanceDiagram() {
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    
    ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) 
            ActivitiUtil.getRepositoryService()).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());

    if (pde != null && pde.isGraphicalNotationDefined()) {
      BpmnModel bpmnModel = ActivitiUtil.getRepositoryService().getBpmnModel(pde.getId());
      ProcessDiagramGenerator diagramGenerator = ((ProcessEngineImpl) ActivitiUtil.getProcessEngine()).getProcessEngineConfiguration().getProcessDiagramGenerator();
      InputStream resource = diagramGenerator.generateDiagram(bpmnModel, "png", ActivitiUtil.getRuntimeService().getActiveActivityIds(processInstance.getId()));

      InputRepresentation output = new InputRepresentation(resource, MediaType.IMAGE_PNG);
      return output;
      
    } else {
      throw new ActivitiIllegalArgumentException("Process instance with id '" + processInstance.getId() + "' has no graphical notation defined.");
    }
  }
}
