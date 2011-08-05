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
package org.activiti.cdi.impl.event;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class EventNotificationTest extends CdiActivitiTestCase {

  @Deployment(resources = {"org/activiti/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testReceiveAll() { 
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    runtimeService.startProcessInstanceByKey("process1");

    // assert that now the bean has received 11 events
    assertEquals(11, listenerBean.getEventsReceived().size());
  }

  @Deployment(resources = { 
      "org/activiti/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml",
      "org/activiti/cdi/impl/event/EventNotificationTest.process2.bpmn20.xml" })
  public void testSelectEventsPerProcessDefinition() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);

    
    assertEquals(0, listenerBean.getEventsReceivedByKey().size());
    //start the 2 processes
    runtimeService.startProcessInstanceByKey("process1");
    runtimeService.startProcessInstanceByKey("process2");

    // assert that now the bean has received 11 events
    assertEquals(11, listenerBean.getEventsReceivedByKey().size());
  }
  
  @Deployment(resources = {"org/activiti/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testSelectEventsPerActivity() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);

    assertEquals(0, listenerBean.getEndActivityService1());
    assertEquals(0, listenerBean.getStartActivityService1());
    assertEquals(0, listenerBean.getTakeTransitiont1());

    // start the process
    runtimeService.startProcessInstanceByKey("process1");

    // assert
    assertEquals(1, listenerBean.getEndActivityService1());
    assertEquals(1, listenerBean.getStartActivityService1());
    assertEquals(1, listenerBean.getTakeTransitiont1());
  }


}
