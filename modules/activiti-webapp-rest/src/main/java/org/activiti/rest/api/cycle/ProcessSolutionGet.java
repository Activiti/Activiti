package org.activiti.rest.api.cycle;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Cycle Webscript returning a {@link ProcessSolution}
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionGet extends ActivitiCycleWebScript {

  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    String processSolutionId = req.getMandatoryString("processSolutionId");
    try {
      ProcessSolution processSolution = processSolutionService.getProcessSolutionById(processSolutionId);
      model.put("processSolution", processSolution);
    } catch (Exception e) {
      throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Could not load ProcessSolution with id '" + processSolutionId + "': " + e.getMessage(),
              e);
    }

    try {
      List<VirtualRepositoryFolder> folders = processSolutionService.getFoldersForProcessSolution(processSolutionId);
      model.put("folders", folders);
    } catch (Exception e) {
      throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Could not load the Folders of the ProcessSolution with id '" + processSolutionId
              + "': " + e.getMessage(), e);
    }

  }

}
