package org.activiti.rest.api.tasks;

import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Returns a task's form.
 *
 * @author Erik Winlof
 */
public class TaskFormGet extends ActivitiWebScript {

  /**
   * Returns a task's form.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String taskId = req.getMandatoryPathParameter("taskId");
    Object taskForm = getTaskService().getRenderedTaskForm(taskId);
    if (taskForm != null) {
      if (taskForm instanceof String) {
        model.put("form", taskForm);
      }
      else {
        throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "The form for task '" + taskId + "' cannot be rendered using the rest api.");
      }
    }
    else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for task '" + taskId + "'.");
    }
  }
}
