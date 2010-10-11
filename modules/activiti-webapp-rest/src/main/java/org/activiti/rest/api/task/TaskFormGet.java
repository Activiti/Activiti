package org.activiti.rest.api.task;

import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * Returns a task's form.
 *
 * @author Erik Winlof
 */
public class TaskFormGet extends ActivitiStreamingWebScript
{

  /**
   * Returns a task form
   *
   * @param req The activiti webscript request
   * @param res The webscript response
   */
  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) {
    String taskId = req.getMandatoryPathParameter("taskId");
    Object form = getTaskService().getRenderedTaskForm(taskId);
    InputStream is = null;
    if (form != null && form instanceof String) {
      is = new ByteArrayInputStream(((String) form).getBytes());
    }
    else if (form != null && form instanceof InputStream) {
      is = (InputStream) form;
    }
    if (is != null) {
      String mimeType = getMimeType(is);
      try {
        streamResponse(res, is, new Date(0), null, true, taskId, mimeType);
      } catch (IOException e) {
        throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "The form for task '" + taskId + "' failed to render.");
      }
    }
    else if (form != null){
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "The form for task '" + taskId + "' cannot be rendered using the rest api.");
    }
    else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for task '" + taskId + "'.");
    }
  }

}
