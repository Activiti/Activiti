package org.activiti.engine.test.api.runtime;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public class DelayedTaskSubmitter extends Thread {

  RuntimeService runtimeService;
  TaskService taskService;


  void setServices(RuntimeService runtimeService, TaskService taskService) {
    this.runtimeService = runtimeService;
    this.taskService = taskService;
  }

  public void run() {
    try {
      Thread.sleep(500);
      Task task = (Task) taskService.createTaskQuery().singleResult();
      taskService.complete(task.getId());
      
      
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
