package org.activiti.rest.api.process;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.activiti.engine.impl.util.IoUtil;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Returns a process definition's form.
 *
 * @author Erik Winlof
 */
public class ProcessDefinitionFormGet extends ActivitiStreamingWebScript
{

  /**
   * Returns a task form
   *
   * @param req The activiti webscript request
   * @param res The webscript response
   */
  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) {
    String processDefinitionId = req.getMandatoryPathParameter("processDefinitionId");
    Object form = getFormService().getRenderedStartForm(processDefinitionId);
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
        streamResponse(res, is, new Date(0), null, true, processDefinitionId, mimeType);
      } catch (IOException e) {
        throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "The form for process definition '" + processDefinitionId + "' failed to render.");
      } finally {
        IoUtil.closeSilently(is);
      }
    }
    else if (form != null) {
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "The form for process definition '" + processDefinitionId + "' cannot be rendered using the rest api.");
    }
    else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for process definition '" + processDefinitionId + "'.");
    }
  }

}
