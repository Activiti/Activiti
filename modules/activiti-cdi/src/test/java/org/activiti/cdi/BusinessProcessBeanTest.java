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
package org.activiti.cdi;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Daniel Meyer
 */
public class BusinessProcessBeanTest extends CdiActivitiTestCase {

  /* General test asserting that the business process bean is functional */
  @Deployment
  public void test() throws Exception {

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    getBeanInstance(Actor.class).setActorId("kermit");

    // start the process
    businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // ensure that the process is started:
    assertNotNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());

    // ensure that there is a single task waiting
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertNotNull(task);

    String value = "value";
    businessProcess.setProcessVariable("key", value);
    assertEquals(value, businessProcess.getProcessVariable("key"));

    // complete the task
    assertEquals(task.getId(), businessProcess.resumeTaskById(task.getId()).getId());
    businessProcess.completeTask();

    // assert the task is completed
    assertNull(processEngine.getTaskService().createTaskQuery().singleResult());

    // assert that the process is ended:
    assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());

  }

  @Deployment(resources = "org/activiti/cdi/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResumeTask() throws Exception {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    getBeanInstance(Actor.class).setActorId("kermit");

    String pid = runtimeService.startProcessInstanceByKey("businessProcessBeanTest").getId();
    businessProcess.resumeProcessById(pid);

    // assert that the business process bean resumes a task, if one task is
    // active in the current process instance.
    businessProcess.completeTask();

    assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());
  }

  @Deployment(resources = "org/activiti/cdi/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testCompleteTask() throws Exception {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    getBeanInstance(Actor.class).setActorId("kermit");

    businessProcess.startProcessByKey("businessProcessBeanTest");
    businessProcess.completeTask();

    // assert that the taskId is null after completing a task.
    assertNull(businessProcess.getTaskId());
  }
  
  @Deployment(resources = "org/activiti/cdi/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveProcessInstanceBean() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    getBeanInstance(Actor.class).setActorId("kermit");

    try {
      getBeanInstance(ProcessInstance.class);
      fail();
    } catch (ActivitiException e) {
      // this should happen
    }

    String pid = businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // assert that now we can resolve the ProcessInstance-bean
    assertEquals(pid, getBeanInstance(ProcessInstance.class).getId());

    businessProcess.completeTask();
  }

  @Deployment(resources = "org/activiti/cdi/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveTaskBean() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    getBeanInstance(Actor.class).setActorId("kermit");

    try {
      getBeanInstance(ProcessInstance.class);
      fail();
    } catch (ActivitiException e) {
      // this should happen
    }

    businessProcess.startProcessByKey("businessProcessBeanTest");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // assert that now we can resolve the Task-bean
    assertEquals(taskId, getBeanInstance(Task.class).getId());

    businessProcess.completeTask();
  }
}
