package org.activiti.rest.api.process;

import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Returns a process definition's form.
 *
 * @author Erik Winlof
 */
public class ProcessDefinitionFormGet extends ActivitiWebScript {

  /**
   * Returns a process definition's form.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String processDefinitionId = req.getMandatoryPathParameter("processDefinitionId");
    Object processDefinitionForm = getTaskService().getRenderedStartFormById(processDefinitionId);
    if (processDefinitionForm != null) {
      if (processDefinitionForm instanceof String) {
        model.put("form", processDefinitionForm);
      }
      else {
        throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "The form for process definition '" + processDefinitionId + "' cannot be rendered using the rest api.");
      }
    }
    else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for process definition '" + processDefinitionId + "'.");
    }
  }
}
