package org.activiti.rest.api.cycle;

import java.util.Map;

import org.activiti.cycle.impl.processsolution.connector.ProcessSolutionConnector;
import org.activiti.rest.api.cycle.dto.TreeFolderDto;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiRequestObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Creates a new process solution
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionPost extends ActivitiCycleWebScript {

  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    ActivitiRequestObject obj = req.getBody();
    String processSolutionName = req.getMandatoryString(obj, "processSolutionName");
    String psId = processSolutionService.createNewProcessSolution(processSolutionName);
    ProcessSolutionConnector psConnector = new ProcessSolutionConnector(psId);
    model.put("folder", new TreeFolderDto(psConnector.getRepositoryFolder("/")));
  }

}
