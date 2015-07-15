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

package org.activiti.engine.test.bpmn.gateway;

import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest extends PluggableActivitiTestCase {

  /**
   * Case where there is a parallel gateway that splits into 3 paths of
   * execution, that are immediately joined, without any wait states in between.
   * In the end, no executions should be in the database.
   */
  @Deployment
  public void testSplitMergeNoWaitstates() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("forkJoinNoWaitStates");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testUnstructuredConcurrencyTwoForks() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoForks");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testUnstructuredConcurrencyTwoJoins() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoJoins");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testForkFollowedByOnlyEndEvents() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("forkFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testNestedForksFollowedByEndEvents() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("nestedForksFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }
  
  // ACT-482
  @Deployment
  public void testNestedForkJoin() {
   runtimeService.startProcessInstanceByKey("nestedForkJoin");
   
   // After process startm, only task 0 should be active
   TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc(); 
   List<Task> tasks = query.list();
   assertEquals(1, tasks.size());
   assertEquals("Task 0", tasks.get(0).getName());
   
   // Completing task 0 will create Task A and B
   taskService.complete(tasks.get(0).getId());
   tasks = query.list();
   assertEquals(2, tasks.size());
   assertEquals("Task A", tasks.get(0).getName());
   assertEquals("Task B", tasks.get(1).getName());
   
   // Completing task A should not trigger any new tasks
   taskService.complete(tasks.get(0).getId());
   tasks = query.list();
   assertEquals(1, tasks.size());
   assertEquals("Task B", tasks.get(0).getName());

   // Completing task B creates tasks B1 and B2
   taskService.complete(tasks.get(0).getId());
   tasks = query.list();
   assertEquals(2, tasks.size());
   assertEquals("Task B1", tasks.get(0).getName());
   assertEquals("Task B2", tasks.get(1).getName());
   
   // Completing B1 and B2 will activate both joins, and process reaches task C
   taskService.complete(tasks.get(0).getId());
   taskService.complete(tasks.get(1).getId());
   tasks = query.list();
   assertEquals(1, tasks.size());
   assertEquals("Task C", tasks.get(0).getName());   
  }
  
  /**
   * https://activiti.atlassian.net/browse/ACT-1222
   */
  @Deployment
  public void testReceyclingExecutionWithCallActivity() {
    runtimeService.startProcessInstanceByKey("parent-process");
    
    // After process start we have two tasks, one from the parent and one from the sub process
    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc(); 
    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("Another task", tasks.get(0).getName());
    assertEquals("Some Task", tasks.get(1).getName());
    
    // we complete the task from the parent process, the root execution is receycled, the task in the sub process is still there 
    taskService.complete(tasks.get(1).getId());
    tasks = query.list();
    assertEquals(1, tasks.size());
    assertEquals("Another task", tasks.get(0).getName());

    // we end the task in the sub process and the sub process instance end is propagated to the parent process 
    taskService.complete(tasks.get(0).getId());
    assertEquals(0, taskService.createTaskQuery().count()); 
    
    // There is a QA config without history, so we cannot work with this:
    //assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).finished().count());    
  }
  
  // Test to verify ACT-1755
  @Deployment
  public void testHistoryTables() {
	  
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testHistoryRecords");
    
    List<HistoricActivityInstance> history = historyService
    		.createHistoricActivityInstanceQuery()
    		.processInstanceId(pi.getId()). list();
    
    for (HistoricActivityInstance h: history) {
    	if (h.getActivityId().equals("parallelgateway2")) {
    		  assertNotNull(h.getEndTime());
    	}
    }
    
  }
  
  @Deployment
  public void testAsyncBehavior() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("async");
    waitForJobExecutorToProcessAllJobs(5000, 500);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
  
  /*@Deployment
  public void testAsyncBehavior() {
    for (int i = 0; i < 100; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("async");
    }
    assertEquals(200, managementService.createJobQuery().count());
    waitForJobExecutorToProcessAllJobs(120000, 5000);
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }*/
    
}
