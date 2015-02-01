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
package org.activiti.cdi.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Daniel Meyer
 */
public class BusinessProcessBeanTest extends CdiActivitiTestCase {

  /* General test asserting that the business process bean is functional */
  @Test
  @Deployment
  public void test() throws Exception {

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // start the process
    businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // ensure that the process is started:
    assertNotNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());

    // ensure that there is a single task waiting
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertNotNull(task);

    String value = "value";
    businessProcess.setVariable("key", value);
    assertEquals(value, businessProcess.getVariable("key"));

    // complete the task
    assertEquals(task.getId(), businessProcess.startTask(task.getId()).getId());
    businessProcess.completeTask();

    // assert the task is completed
    assertNull(processEngine.getTaskService().createTaskQuery().singleResult());

    // assert that the process is ended:
    assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());

  }
  
  @Test
  @Deployment
  public void testProcessWithoutWaitState() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // start the process
    businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // assert that the process is ended:
    assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());
  }

  @Test
  @Deployment(resources = "org/activiti/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveProcessInstanceBean() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    assertNull(getBeanInstance(ProcessInstance.class));
    assertNull(getBeanInstance("processInstanceId"));
    assertNull(getBeanInstance(Execution.class));
    assertNull(getBeanInstance("executionId"));

    String pid = businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // assert that now we can resolve the ProcessInstance-bean
    assertEquals(pid, getBeanInstance(ProcessInstance.class).getId());
    assertEquals(pid, getBeanInstance("processInstanceId"));
    assertEquals(pid, getBeanInstance(Execution.class).getId());
    assertEquals(pid, getBeanInstance("executionId"));

    taskService.complete(taskService.createTaskQuery().singleResult().getId());
  }

  @Test
  @Deployment(resources = "org/activiti/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveTaskBean() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    assertNull(getBeanInstance(Task.class));
    assertNull(getBeanInstance("taskId"));
    

    businessProcess.startProcessByKey("businessProcessBeanTest");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    
    businessProcess.startTask(taskId);

    // assert that now we can resolve the Task-bean
    assertEquals(taskId, getBeanInstance(Task.class).getId());
    assertEquals(taskId, getBeanInstance("taskId"));

    taskService.complete(taskService.createTaskQuery().singleResult().getId());
  }
}
