package org.activiti.rest.api.process;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class ProcessInstanceDiagramGet extends ActivitiStreamingWebScript {

  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) throws IOException {
    String processInstanceId = req.getMandatoryPathParameter("processInstanceId");

    ExecutionEntity pi =
        (ExecutionEntity) getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

    if (pi == null) {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "Process instance with id" + processInstanceId + " could not be found");
    }

    ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) getRepositoryService())
        .getDeployedProcessDefinition(pi.getProcessDefinitionId());

    if (pde != null && pde.isGraphicalNotationDefined()) {
      InputStream resource = ProcessDiagramGenerator.generateDiagram(pde, "png", getRuntimeService().getActiveActivityIds(processInstanceId));

      try {
        streamResponse(res, resource, new Date(0), null, true, "diagram.png", "image/png");
      } catch (IOException e) {
        throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Diagram for process instance with id" + processInstanceId + " could not be streamed: " + e.getMessage());
      } finally {
        IoUtil.closeSilently(resource);
      }
    } else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "Process instance with id " + processInstanceId + " has no graphic description");
    }
  }

}
