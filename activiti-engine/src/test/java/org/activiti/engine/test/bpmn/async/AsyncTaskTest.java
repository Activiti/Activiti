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
import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;

/**
 * 
 * @author Daniel Meyer
 */
public class AsyncTaskTest extends PluggableActivitiTestCase {

  public static boolean INVOCATION;

  @Deployment
  public void testAsyncServiceNoListeners() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service was invoked
    assertTrue(INVOCATION);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceListeners() {
    String pid = runtimeService.startProcessInstanceByKey("asyncService").getProcessInstanceId();
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceConcurrent() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

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

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service was invoked
    assertTrue(INVOCATION);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testFailingAsyncServiceTimer() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database, and it is a message
    assertEquals(1, managementService.createJobQuery().count());
    Job job = managementService.createJobQuery().singleResult();

    try {
      managementService.executeJob(job.getId());
      fail();
    } catch (Exception e) {
      // exception expected
    }

    // the service failed: the execution is still sitting in the service task:
    Execution execution = null;
    for (Execution e : runtimeService.createExecutionQuery().list()) {
      if (e.getParentId() != null) {
        execution = e;
      }
    }
    assertNotNull(execution);
    assertEquals("service", runtimeService.getActiveActivityIds(execution.getId()).get(0));

    // there is still a single job because the timer was created in the same
    // transaction as the service was executed (which rolled back)
    assertEquals(1, managementService.createTimerJobQuery().count());

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
    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service failed: the execution is still sitting in the service
    // task:
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertNotNull(execution);
    assertEquals("service", runtimeService.getActiveActivityIds(execution.getId()).get(0));

    // there are tow jobs, the message and the timer (the message will not
    // be retried anymore, max retires is reached.)
    assertEquals(2, managementService.createJobQuery().count());

    // now the timer triggers:
    Context.getProcessEngineConfiguration().getClock().setCurrentTime(new Date(System.currentTimeMillis() + 10000));
    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // and we are done:
    assertNull(runtimeService.createExecutionQuery().singleResult());
    // and there are no more jobs left:
    assertEquals(0, managementService.createJobQuery().count());

  }

  @Deployment
  public void testAsyncServiceSubProcessTimer() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be two jobs in the database:
    assertEquals(1, managementService.createJobQuery().count());
    
    // the service was not invoked:
    assertFalse(INVOCATION);

    waitForJobExecutorToProcessAllJobs(5000L, 200L);
    
    // the service was invoked
    assertTrue(INVOCATION);
    
    // both the timer and the message are cancelled
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceSubProcess() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    assertEquals(1, managementService.createJobQuery().count());

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // both the timer and the message are cancelled
    assertEquals(0, managementService.createJobQuery().count());

  }

  @Deployment
  public void testAsyncTask() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncTask");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());

    waitForJobExecutorToProcessAllJobs(5000L, 200L);

    // the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }
  
  @Deployment
  public void testAsyncEndEvent() {  
    // start process 
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncEndEvent");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    
    Object value = runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    assertNull(value);
    
    waitForJobExecutorToProcessAllJobs(2000L, 200L);
    
    // the job is done
    assertEquals(0, managementService.createJobQuery().count());
    
    assertProcessEnded(processInstance.getId());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
      assertEquals(3, variables.size());
      
      Object historyValue = null;
      for (HistoricVariableInstance variable : variables) {
        if ("variableSetInExecutionListener".equals(variable.getVariableName())) {
          historyValue = variable.getValue();
        }
      }
      assertEquals("firstValue", historyValue);
    }
  }

  @Deployment
  public void testAsyncScript() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncScript");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the script was not invoked:
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    String eid = null;
    for (Execution e : executions) {
      if (e.getParentId() != null) {
        eid = e.getId();
      }
    }
    assertNull(runtimeService.getVariable(eid, "invoked"));

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());

    // the script was invoked
    assertEquals("true", runtimeService.getVariable(eid, "invoked"));

    runtimeService.trigger(eid);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsyncCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsyncServiceNoListeners.bpmn20.xml" })
  public void testAsyncCallActivity() throws Exception {
    // start process
    runtimeService.startProcessInstanceByKey("asyncCallactivity");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());

    waitForJobExecutorToProcessAllJobs(20000L, 250L);

    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testBasicAsyncCallActivity.bpmn20.xml", "org/activiti/engine/test/bpmn/StartToEndTest.testStartToEnd.bpmn20.xml" })
  public void testBasicAsyncCallActivity() {
    runtimeService.startProcessInstanceByKey("myProcess");
    Assert.assertEquals("There should be one job available.", 1, managementService.createJobQuery().count());
    waitForJobExecutorToProcessAllJobs(5000L, 250L);
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncUserTask() {
    // start process
    String pid = runtimeService.startProcessInstanceByKey("asyncUserTask").getId();
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // the task listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "taskListener"));
    // there is no usertask
    assertNull(taskService.createTaskQuery().singleResult());

    waitForJobExecutorToProcessAllJobs(5000L, 250L);
    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // the task listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "taskListener"));

    // there is a usertask
    assertNotNull(taskService.createTaskQuery().singleResult());
    // and no more job
    assertEquals(0, managementService.createJobQuery().count());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

  }

  @Deployment
  public void testMultiInstanceAsyncTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");
    
    // now there should be one job in the database:
    assertEquals(3, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());

    // execute first of 3 parallel multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).list().get(0).getId());
    assertEquals(2, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());
    
    // execute second of 3 parallel multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).list().get(0).getId());
    assertEquals(1, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());
    
    // execute third of 3 parallel multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());

    // the job is done
    assertEquals(0, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      
      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;
        
        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;
        
        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;
        
        } else {
          Assert.fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }
      
      assertEquals(1, startCount);
      assertEquals(3, taskCount);
      assertEquals(1, endCount);
    }
  }
  
  @Deployment
  public void testMultiInstanceTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      
      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;
        
        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;
        
        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;
        
        } else {
          Assert.fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }
      
      assertEquals(1, startCount);
      assertEquals(3, taskCount);
      assertEquals(1, endCount);
    }
  }
  
  @Deployment
  public void testMultiInstanceAsyncSequentialTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");
    
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());

    // execute first of 3 sequential multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());
    
    // execute second of 3 sequential multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());
    
    // execute third of 3 sequential multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    
    // the job is done
    assertEquals(0, managementService.createJobQuery().processInstanceId(processInstance.getId()).count());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      
      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;
        
        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;
        
        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;
        
        } else {
          Assert.fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }
      
      assertEquals(1, startCount);
      assertEquals(3, taskCount);
      assertEquals(1, endCount);
    }
  }
  
  @Deployment
  public void testMultiInstanceSequentialTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      
      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;
        
        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;
        
        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;
        
        } else {
          Assert.fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }
      
      assertEquals(1, startCount);
      assertEquals(3, taskCount);
      assertEquals(1, endCount);
    }
  }

}
