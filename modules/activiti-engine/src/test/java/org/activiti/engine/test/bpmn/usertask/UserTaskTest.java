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

package org.activiti.engine.test.bpmn.usertask;

import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class UserTaskTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testTaskPropertiesNotNull() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task.getId());
    assertEquals("my task", task.getName());
    assertEquals("Very important", task.getDescription());
    assertTrue(task.getPriority() > 0);
    assertEquals("kermit", task.getAssignee());
    assertEquals(processInstance.getId(), task.getProcessInstanceId());
    assertEquals(processInstance.getId(), task.getExecutionId());
    assertNotNull(task.getProcessDefinitionId());
    assertNotNull(task.getTaskDefinitionKey());
    assertNotNull(task.getCreateTime());
    
    // the next test verifies that if an execution creates a task, that no events are created during creation of the task.
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(0, taskService.getTaskEvents(task.getId()).size());
    }
  }
  
  @Deployment
  public void testQuerySortingWithParameter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().size());
  }
  
  @Deployment
  public void testCompleteAfterParallelGateway() throws InterruptedException {
	  // related to http://jira.codehaus.org/browse/ACT-1054
	  
	  // start the process
    runtimeService.startProcessInstanceByKey("ForkProcess");
    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(2, taskList.size());
	
    // make sure user task exists
    Task task = taskService.createTaskQuery().taskDefinitionKey("SimpleUser").singleResult();
  	assertNotNull(task);
	
  	// attempt to complete the task and get PersistenceException pointing to "referential integrity constraint violation"
  	taskService.complete(task.getId());
	}
  
  @Deployment
  public void testTaskCategory() {
  	runtimeService.startProcessInstanceByKey("testTaskCategory");
  	Task task = taskService.createTaskQuery().singleResult();
  	
  	// Test if the property set in the model is shown in the task
  	String testCategory = "My Category";
  	assertEquals(testCategory, task.getCategory());
  	
  	// Test if can be queried by query API
  	assertEquals("Task with category", taskService.createTaskQuery().taskCategory(testCategory).singleResult().getName());
  	assertTrue(taskService.createTaskQuery().taskCategory("Does not exist").count() == 0);
  	
  	if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
  		// Check historic task
  		HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
  		assertEquals(testCategory, historicTaskInstance.getCategory());
  		assertEquals("Task with category", historyService.createHistoricTaskInstanceQuery().taskCategory(testCategory).singleResult().getName());
  		assertTrue(historyService.createHistoricTaskInstanceQuery().taskCategory("Does not exist").count() == 0);
  		
  		// Update category
  		String newCategory = "New Test Category";
  		task.setCategory(newCategory);
  		taskService.saveTask(task);
  		
  		task = taskService.createTaskQuery().singleResult();
  		assertEquals(newCategory, task.getCategory());
  		assertEquals("Task with category", taskService.createTaskQuery().taskCategory(newCategory).singleResult().getName());
  		assertTrue(taskService.createTaskQuery().taskCategory(testCategory).count() == 0);
  		
  		// Complete task and verify history
  		taskService.complete(task.getId());
  		historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
  		assertEquals(newCategory, historicTaskInstance.getCategory());
  		assertEquals("Task with category", historyService.createHistoricTaskInstanceQuery().taskCategory(newCategory).singleResult().getName());
  		assertTrue(historyService.createHistoricTaskInstanceQuery().taskCategory(testCategory).count() == 0);
  	}
  }
  
}
