package org.activiti.rest.api.task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

public class TaskFormResource extends SecuredResource {
  
  @Get
  public InputRepresentation getTaskForm() {
    if(authenticate() == false) return null;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    Object form = ActivitiUtil.getFormService().getRenderedTaskForm(taskId);
    InputStream is = null;
    if (form != null && form instanceof String) {
      is = new ByteArrayInputStream(((String) form).getBytes());
    }
    else if (form != null && form instanceof InputStream) {
      is = (InputStream) form;
    }
    if (is != null) {
      InputRepresentation output = new InputRepresentation(is);
      return output;
    
    } else if (form != null){
      throw new ActivitiException("The form for task '" + taskId + "' cannot be rendered using the rest api.");
    
    } else {
      throw new ActivitiException("There is no form for task '" + taskId + "'.");
    }
  }
}
