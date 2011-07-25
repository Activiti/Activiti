package org.activiti.rest.api.task;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.AbstractPaginateList;
import org.activiti.rest.api.ActivitiUtil;

public class TasksPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<TaskResponse> responseList = new ArrayList<TaskResponse>();
    for (Object task : list) {
      TaskResponse taskResponse = new TaskResponse((Task) task);
      TaskFormData taskFormData = ActivitiUtil.getFormService().getTaskFormData(taskResponse.getId());
      if(taskFormData != null) {
        taskResponse.setFormResourceKey(taskFormData.getFormKey());     
      }
      responseList.add(taskResponse);
    }
    return responseList;
  }
}
