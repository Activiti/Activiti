package org.activiti.cdi.impl.task;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.task.Task;


public class CdiTaskServiceTest extends CdiActivitiTestCase {
  
  public void testClaimTask() {
    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);
    taskService.claim(newTask.getId(), "kermit");
    taskService.deleteTask(newTask.getId(),true);
  }

}
