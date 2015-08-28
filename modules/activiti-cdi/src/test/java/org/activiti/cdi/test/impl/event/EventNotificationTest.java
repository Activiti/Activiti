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
package org.activiti.cdi.test.impl.event;

import static org.junit.Assert.assertEquals;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class EventNotificationTest extends CdiActivitiTestCase {

  @Test
  @Deployment(resources = {"org/activiti/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testReceiveAll() { 
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    runtimeService.startProcessInstanceByKey("process1");

    // assert that now the bean has received 11 events
    assertEquals(11, listenerBean.getEventsReceived().size());
  }

  @Test
  @Deployment(resources = { 
      "org/activiti/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml",
      "org/activiti/cdi/test/impl/event/EventNotificationTest.process2.bpmn20.xml" })
  public void testSelectEventsPerProcessDefinition() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();
    
    assertEquals(0, listenerBean.getEventsReceivedByKey().size());
    //start the 2 processes
    runtimeService.startProcessInstanceByKey("process1");
    runtimeService.startProcessInstanceByKey("process2");

    // assert that now the bean has received 11 events
    assertEquals(11, listenerBean.getEventsReceivedByKey().size());
  }
  
  @Test
  @Deployment(resources = {"org/activiti/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testSelectEventsPerActivity() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();
    
    assertEquals(0, listenerBean.getEndActivityService1());
    assertEquals(0, listenerBean.getStartActivityService1WithoutLoopCounter());
    assertEquals(0, listenerBean.getTakeTransitiont1());

    // start the process
    runtimeService.startProcessInstanceByKey("process1");

    // assert
    assertEquals(1, listenerBean.getEndActivityService1());
    assertEquals(1, listenerBean.getStartActivityService1WithoutLoopCounter());
    assertEquals(1, listenerBean.getTakeTransitiont1());
  }
  
  @Test
  @Deployment(resources = {"org/activiti/cdi/test/impl/event/TaskEventNotificationTest.process3.bpmn20.xml"})
  public void testCreateEventsPerActivity() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();
    
    assertEquals(0, listenerBean.getCreateTask1());
    assertEquals(0, listenerBean.getAssignTask1());
    assertEquals(0, listenerBean.getCompleteTask1());
    assertEquals(0, listenerBean.getDeleteTask3());
    
    // start the process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process3");
    
    Task task = taskService.createTaskQuery().singleResult();
    
    taskService.claim(task.getId(), "auser");
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    do {
      task = taskService.createTaskQuery().singleResult();
    } while (task == null);
    
    runtimeService.deleteProcessInstance(pi.getId(), "DELETED");
    
    // assert
    assertEquals(1, listenerBean.getCreateTask1());
    assertEquals(1, listenerBean.getCreateTask2());
    assertEquals(1, listenerBean.getAssignTask1());
    assertEquals(1, listenerBean.getCompleteTask1());
    assertEquals(1, listenerBean.getCompleteTask2());
    assertEquals(1, listenerBean.getDeleteTask3());
  }


}
