package org.activiti.rest.api.tasks;

import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Returns info about a task.
 *
 * @author Erik Winlšf
 */
public class TaskGet extends ActivitiWebScript {


  /**
   * Colelcts details about a task for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String taskId = getMandatoryPathParameter(req, "taskId");
    model.put("task", getTaskService().findTask(taskId));
  }
}
