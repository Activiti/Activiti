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

package org.activiti.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 */
public class HistoricTaskInstanceUpdateTest extends PluggableActivitiTestCase {

  
  @Deployment
  public void testHistoricTaskInstanceUpdate() {
    runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();
    
    Task task = taskService.createTaskQuery().singleResult();
    
    // Update and save the task's fields before it is finished
    task.setPriority(12345);
    task.setDescription("Updated description");
    task.setName("Updated name");
    task.setAssignee("gonzo");
    taskService.saveTask(task);   

    taskService.complete(task.getId());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals("Updated name", historicTaskInstance.getName());
    assertEquals("Updated description", historicTaskInstance.getDescription());
    assertEquals("gonzo", historicTaskInstance.getAssignee());
    assertEquals("task", historicTaskInstance.getTaskDefinitionKey());

    
    // Validate fix of ACT-1923: updating assignee to null should be reflected in history
    ProcessInstance secondInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
    
    task = taskService.createTaskQuery().singleResult();
    
    task.setDescription(null);
    task.setName(null);
    task.setAssignee(null);
    taskService.saveTask(task);   

    taskService.complete(task.getId());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processInstanceId(secondInstance.getId()).count());

    historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(secondInstance.getId()).singleResult();
    assertNull(historicTaskInstance.getName());
    assertNull(historicTaskInstance.getDescription());
    assertNull(historicTaskInstance.getAssignee());
  }

    @Deployment
    public void testHistoricTaskInstanceExecutionIdUpdateOnConcurrentExecution() {

	    ProcessInstance pi = runtimeService.startProcessInstanceByKey("concurrencyProcess");
	    Task task = taskService.createTaskQuery().singleResult();
	    assertThat(historyService.createHistoricTaskInstanceQuery().count()).isEqualTo(1);

	    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
	    String parentExecutionId = historicTaskInstance.getExecutionId();
	    taskService.complete(task.getId());
	    TaskQuery query = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc();
	    List<Task> tasks = query.list();
	    assertThat(tasks).hasSize(2);

        // the tasks are ordered by name
	    Task task1 = tasks.get(0);
	    assertThat(task1.getName()).isEqualTo("UserA");
	    assertThat(task1.getExecutionId()).isNotEqualTo(parentExecutionId);
	    Task task2 = tasks.get(1);
	    assertThat(task2.getName()).isEqualTo("UserC");
	    assertThat(task2.getExecutionId()).isNotEqualTo(parentExecutionId);

        // terminating one flow from concurrent execution
	    taskService.complete(tasks.get(1).getId());
	    Task userTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
	    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(pi.getId()).list();
	    assertThat(historicTaskInstances).hasSize(3);
	    HistoricTaskInstance activeHistoricTaskInstance = null;
	    for (HistoricTaskInstance historicTaskInst : historicTaskInstances) {
		    if (userTask.getId().equals(historicTaskInst.getId())) {
			    activeHistoricTaskInstance = historicTaskInst;
			    break;
			}
		}
	    assertThat(parentExecutionId).isEqualTo(activeHistoricTaskInstance.getExecutionId());
  }
}
