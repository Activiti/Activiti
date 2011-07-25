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
