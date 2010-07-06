package org.activiti.rest.api.process;

import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Returns details about a process definition.
 *
 * @author Erik Winlöf
 */
public class ProcessDefinitionGet extends ActivitiWebScript
{

  /**
   * Returns details about a process definition.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String processDefinitionId = getMandatoryPathParameter(req, "processDefinitionId");
    model.put("processDefinition", getProcessService().findProcessDefinitionById(processDefinitionId));
  }

}
