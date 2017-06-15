package org.activiti.engine.test.bpmn.event.timer.compatibility;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class BoundaryTimerEventRepeatCompatibilityTest extends TimerEventCompatibilityTest {

  @Deployment
  public void testRepeatWithoutEnd() throws Throwable {

    Calendar calendar = Calendar.getInstance();
    Date baseTime = calendar.getTime();

    calendar.add(Calendar.MINUTE, 20);
    // expect to stop boundary jobs after 20 minutes
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    DateTime dt = new DateTime(calendar.getTime());
    String dateStr = fmt.print(dt);

    // reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

    runtimeService.setVariable(processInstance.getId(), "EndDateForBoundary", dateStr);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    Task task = tasks.get(0);
    assertEquals("Task A", task.getName());

    // Test Boundary Events
    // complete will cause timer to be created
    taskService.complete(task.getId());

    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());

    // change the job in old mode (the configuration should not be json in
    // "old mode" but a simple string).
    TimerJobEntity job = (TimerJobEntity) jobs.get(0);
    changeConfigurationToPlainText(job);

    // boundary events

    waitForJobExecutorToProcessAllJobs(2000, 100);
    
    // a new job must be prepared because there are 10 repeats 2 seconds interval
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());

    for (int i = 0; i < 9; i++) {
      nextTimeCal.add(Calendar.SECOND, 2);
      processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());
      waitForJobExecutorToProcessAllJobs(2000, 100);
      // a new job must be prepared because there are 10 repeats 2 seconds interval
      
      jobs = managementService.createTimerJobQuery().list();
      assertEquals(1, jobs.size());
    }

    nextTimeCal.add(Calendar.SECOND, 2);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 100);
    } catch (Exception ex) {
      fail("Should not have any other jobs because the endDate is reached");
    }
    
    tasks = taskService.createTaskQuery().list();
    task = tasks.get(0);
    assertEquals("Task B", task.getName());
    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
    } catch (Exception e) {
      fail("No jobs should be active here.");
    }

    // now All the process instances should be completed
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, processInstances.size());

    // no jobs
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(0, jobs.size());

    // no tasks
    tasks = taskService.createTaskQuery().list();
    assertEquals(0, tasks.size());

  }

}
