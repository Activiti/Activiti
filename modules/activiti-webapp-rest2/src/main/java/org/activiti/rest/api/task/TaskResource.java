package org.activiti.rest.api.task;

import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class TaskResource extends SecuredResource {
  
  @Get
  public TaskResponse getTasks() {
    if(authenticate() == false) return null;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    TaskResponse response = new TaskResponse(task);
    
    TaskFormData taskFormData = ActivitiUtil.getFormService().getTaskFormData(taskId);
    if(taskFormData != null) {
      response.setFormResourceKey(taskFormData.getFormKey());     
    }
    
    return response;
  }

}
