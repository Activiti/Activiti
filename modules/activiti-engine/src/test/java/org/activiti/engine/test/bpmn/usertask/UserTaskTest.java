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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
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
	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ForkProcess");
	List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
	Set<String> executionIds = new HashSet<String>();
	for (Execution execution: executions)
		executionIds.add(execution.getId());
	
	  // make sure user task exists
	List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("SimpleUser").list();
	
	List<Task> selectedTasks = new ArrayList<Task>();
	for (Task t: tasks)
		if (executionIds.contains(t.getExecutionId()))
			selectedTasks.add(t);
			
	assertEquals(selectedTasks.size(), 1);
	
	// attempt to complete the task and get PersistenceException pointing to "referential integrity constraint violation"
	taskService.complete(selectedTasks.get(0).getId());
	}
	
  
}
