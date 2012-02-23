/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.bpmn.async;

import java.util.Date;

import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;

/**
 * 
 * @author Daniel Meyer
 */
public class AsyncTaskTest extends PluggableActivitiTestCase {
  
  public static boolean INVOCATION;
  
  @Deployment
  public void testAsycServiceNoListeners() {  
    INVOCATION = false;
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the service was invoked
    assertTrue(INVOCATION);    
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());       
  }
  
  @Deployment
  public void testAsycServiceListeners() {  
    String pid = runtimeService.startProcessInstanceByKey("asyncService").getProcessInstanceId();
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    assertEquals(0, managementService.createJobQuery().count());
  }
  
  @Deployment
  public void testAsycServiceConcurrent() {  
    INVOCATION = false;
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the service was invoked
    assertTrue(INVOCATION);    
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());   
  }
  
  @Deployment
  public void testAsyncServiceMultiInstance() {  
    INVOCATION = false;
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the service was invoked
    assertTrue(INVOCATION);    
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());   
  }
  

  @Deployment
  public void testFailingAsycServiceTimer() { 
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database, and it is a message
    assertEquals(1, managementService.createJobQuery().count());
    Job job = managementService.createJobQuery().singleResult();
    if(!(job instanceof MessageEntity)) {
      fail("the job must be a message");
    }      
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the service failed: the execution is still sitting in the service task:
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertNotNull(execution);
    assertEquals("service", runtimeService.getActiveActivityIds(execution.getId()).get(0));
    
    // there is still a single job because the timer was created in the same transaction as the 
    // service was executed (which rolled back)
    assertEquals(1, managementService.createJobQuery().count());    
    
    runtimeService.deleteProcessInstance(execution.getId(), "dead");        
  }
  
  // TODO: Think about this: 
  @Deployment
  public void FAILING_testFailingAsycServiceTimer() { 
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there are two jobs the message and a timer:
    assertEquals(2, managementService.createJobQuery().count());          
    
    // let 'max-retires' on the message be reached
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the service failed: the execution is still sitting in the service task:
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertNotNull(execution);
    assertEquals("service", runtimeService.getActiveActivityIds(execution.getId()).get(0));
    
    // there are tow jobs, the message and the timer (the message will not be retried anymore, max retires is reached.)
    assertEquals(2, managementService.createJobQuery().count());    
      
    // now the timer triggers:
    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis()+10000));
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // and we are done:
    assertNull(runtimeService.createExecutionQuery().singleResult());    
    // and there are no more jobs left:
    assertEquals(0, managementService.createJobQuery().count());   
        
  }
  
  @Deployment
  public void testAsycServiceSubProcessTimer() { 
    INVOCATION = false;
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be two jobs in the database:
    assertEquals(2, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the service was invoked
    assertTrue(INVOCATION);    
    // both the timer and the message are cancelled
    assertEquals(0, managementService.createJobQuery().count());   
        
  }
  
  @Deployment
  public void testAsycServiceSubProcess() {    
    // start process 
    runtimeService.startProcessInstanceByKey("asyncService");

    assertEquals(1, managementService.createJobQuery().count());
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // both the timer and the message are cancelled
    assertEquals(0, managementService.createJobQuery().count());   
        
  }

  @Deployment
  public void testAsycTask() {  
    // start process 
    runtimeService.startProcessInstanceByKey("asyncTask");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
       
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // the job is done
    assertEquals(0, managementService.createJobQuery().count()); 
  }

  @Deployment
  public void testAsycScript() {  
    // start process 
    runtimeService.startProcessInstanceByKey("asyncScript").getProcessInstanceId();
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the script was not invoked:
    String eid = runtimeService.createExecutionQuery().singleResult().getId();
    assertNull(runtimeService.getVariable(eid, "invoked"));  
    
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  
    // the script was invoked
    assertEquals("true", runtimeService.getVariable(eid, "invoked"));  
    
    runtimeService.signal(eid);        
  }
  
  @Deployment(resources={"org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsycCallActivity.bpmn20.xml", 
          "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsycServiceNoListeners.bpmn20.xml"})
  public void testAsycCallActivity() {  
    // start process 
    runtimeService.startProcessInstanceByKey("asyncCallactivity");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
   
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    
    assertEquals(0, managementService.createJobQuery().count());
      
  }
  
  @Deployment
  public void testAsyncUserTask() {  
    // start process 
    String pid = runtimeService.startProcessInstanceByKey("asyncUserTask").getProcessInstanceId();
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no usertask
    assertNull(taskService.createTaskQuery().singleResult());
        
    waitForJobExecutorToProcessAllJobs(10000L, 25L);
    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    
    // there is a usertask
    assertNotNull(taskService.createTaskQuery().singleResult());
    // and no more job    
    assertEquals(0, managementService.createJobQuery().count());
    
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
    
  }
  

}
