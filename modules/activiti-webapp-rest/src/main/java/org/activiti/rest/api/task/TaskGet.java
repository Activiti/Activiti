package org.activiti.rest.api.task;

import java.util.Map;

import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.rest.model.RestTask;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Returns info about a task.
 *
 * @author Erik Winlof
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
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String taskId = req.getMandatoryPathParameter("taskId");
    TaskEntity task = (TaskEntity) getTaskService().createTaskQuery().taskId(taskId).singleResult();
    RestTask restTask = new RestTask(task);
    
    TaskFormData taskFormData = getFormService().getTaskFormData(taskId);
    if(taskFormData != null) {
      restTask.setFormResourceKey(taskFormData.getFormKey());     
    }
    
    model.put("task", restTask);
  }
}
