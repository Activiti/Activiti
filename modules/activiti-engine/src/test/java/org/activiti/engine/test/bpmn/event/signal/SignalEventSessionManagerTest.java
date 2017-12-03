package org.activiti.engine.test.bpmn.event.signal;

import java.util.Date;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class SignalEventSessionManagerTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSignalRaceConditionsAfterParallelGateway() {
    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsAfterParallelGateway");
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
  @Deployment
  public void testSignalRaceConditionsAfterParallelGatewayAsync() {
    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsAfterParallelGatewayAsync");
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }  

  
  @Deployment
  public void testSignalRaceConditionsThrowCatchSequence() {
    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsThrowCatchSequence");

    taskService.complete(taskService.createTaskQuery().taskName("Task 3").singleResult().getId());
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }  
  
  @Deployment
  public void testSignalRaceConditionsThrowCatchAfterUserTask() {
    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsThrowCatchAfterUserTask");
    
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId());
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }    
  
  @Deployment
  public void testSignalRaceConditionsCatchThrowWithSubProcesses() {
    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsCatchThrowWithSubProcesses");

    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId());
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }      
  
  @Deployment
  public void testSignalThrowCatchUserTask() {
    runtimeService.startProcessInstanceByKey("testSignalThrowCatchUserTask");

    taskService.complete(taskService.createTaskQuery().taskName("User Task").singleResult().getId());
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }      

  @Deployment
  public void testSignalThrowCatchBoundaryUserTask() throws InterruptedException {
    // Set the clock fixed
    Date startTime = new Date();

    runtimeService.startProcessInstanceByKey("testSignalThrowCatchBoundaryUserTask");

    // After setting the clock to startTime + '2 seconds', the timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + 2000));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
     
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }      

    
  @Deployment
  public void testSignalThrowCatchEventGateway() throws InterruptedException {
    // Set the clock fixed
    Date startTime = new Date();

    runtimeService.startProcessInstanceByKey("testSignalThrowCatchEventGateway");

    taskService.complete(taskService.createTaskQuery().taskName("User Task").singleResult().getId());
    
//    // After setting the clock to startTime + '2 seconds', the timer should fire
//    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + 2000));
//    waitForJobExecutorToProcessAllJobs(5000L, 25L);
     
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }      

  @Deployment
  public void testSignalThrowCatchUserTaskGlobal() {
    runtimeService.startProcessInstanceByKey("testSignalThrowCatchUserTaskGlobal");

    taskService.complete(taskService.createTaskQuery().taskName("User Task").singleResult().getId());
    
    // Should wait on global signal catch event activity execution
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(2, runtimeService.createExecutionQuery().count());
  }       

}
