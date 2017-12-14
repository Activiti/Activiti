package org.activiti.engine.test.bpmn.event.signal;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class ThrowSignalEventSessionManagerTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSignalRaceConditionsAfterParallelGateway() {

    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsAfterParallelGateway");
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  public void testSignalRaceConditionsAfterParallelGatewayGlobal() {

    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsAfterParallelGatewayGlobal");
    
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
  public void testSignalRaceConditionsAfterParallelGatewayGlobalAsync() {

    runtimeService.startProcessInstanceByKey("testSignalRaceConditionsAfterParallelGatewayGlobalAsync");

    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }  
  
  @Deployment
  public void testSignalThrowCatchEventGateway() throws InterruptedException {

    runtimeService.startProcessInstanceByKey("testSignalThrowCatchEventGateway");

    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
}
