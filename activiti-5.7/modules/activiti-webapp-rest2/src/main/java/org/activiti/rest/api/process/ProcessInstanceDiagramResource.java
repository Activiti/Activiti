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

package org.activiti.rest.api.process;

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class ProcessInstanceDiagramResource extends SecuredResource {
  
  @Get
  public InputRepresentation getInstanceDiagram() {
    if(authenticate() == false) return null;
    
    String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
    
    if(processInstanceId == null) {
      throw new ActivitiException("No process instance id provided");
    }

    ExecutionEntity pi =
        (ExecutionEntity) ActivitiUtil.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

    if (pi == null) {
      throw new ActivitiException("Process instance with id" + processInstanceId + " could not be found");
    }

    ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) ActivitiUtil.getRepositoryService())
        .getDeployedProcessDefinition(pi.getProcessDefinitionId());

    if (pde != null && pde.isGraphicalNotationDefined()) {
      InputStream resource = ProcessDiagramGenerator.generateDiagram(pde, "png", ActivitiUtil.getRuntimeService().getActiveActivityIds(processInstanceId));

      InputRepresentation output = new InputRepresentation(resource);
      return output;
      
    } else {
      throw new ActivitiException("Process instance with id " + processInstanceId + " has no graphic description");
    }
  }
}
