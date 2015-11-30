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
package org.activiti.engine.test.api.event;

import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to activities.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class HistoricActivityEventsTest extends PluggableActivitiTestCase {

  private TestHistoricActivityEventListener listener;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    this.listener = new TestHistoricActivityEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {

    if (listener != null) {
      listener.clearEventsReceived();
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }

    super.tearDown();
  }

  /**
   * Test added to assert the historic activity instance event
   */
  @Deployment
  public void testHistoricActivityEventDispatched() {
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
    
  	 ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestActivityEvents");
     assertNotNull(processInstance);

     for (int i=0; i<2; i++) {
    	 taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
     }
     
     List<ActivitiEvent> events = listener.getEventsReceived();
     
     // Process instance start
     assertEquals(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED, events.get(0).getType());
     
     // main start
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(1).getType());
     assertEquals("mainStart",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(1)).getEntity()).getActivityId()));
     
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(2).getType());
     assertEquals("mainStart",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(2)).getEntity()).getActivityId()));
     assertNotNull("mainStart",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(2)).getEntity()).getEndTime()));
     
     // Subprocess start
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(3).getType());
     assertEquals("subProcess",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(3)).getEntity()).getActivityId()));
     
     // subProcessStart
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(4).getType());
     assertEquals("subProcessStart",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(4)).getEntity()).getActivityId()));
     
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(5).getType());
     assertEquals("subProcessStart",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(5)).getEntity()).getActivityId()));
     assertNotNull("subProcessStart",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(5)).getEntity()).getEndTime()));
     
     // Task a
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(6).getType());
     assertEquals("a",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(6)).getEntity()).getActivityId()));
     
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(7).getType());
     assertEquals("a",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(7)).getEntity()).getActivityId()));
     assertNotNull("a",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(7)).getEntity()).getEndTime()));
     
     // Task b
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(8).getType());
     assertEquals("b",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(8)).getEntity()).getActivityId()));
     
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(9).getType());
     assertEquals("b",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(9)).getEntity()).getActivityId()));
     assertNotNull("b",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(9)).getEntity()).getEndTime()));
     
     // subProcessEnd
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(10).getType());
     assertEquals("subprocessEnd",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(10)).getEntity()).getActivityId()));
     
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(11).getType());
     assertEquals("subprocessEnd",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(11)).getEntity()).getActivityId()));
     assertNotNull("subprocessEnd",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(11)).getEntity()).getEndTime()));
     
     // subProcess end
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(12).getType());
     assertEquals("subProcess",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(12)).getEntity()).getActivityId()));
     assertNotNull("subProcess",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(12)).getEntity()).getEndTime()));
     
     // main end
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, events.get(13).getType());
     assertEquals("mainEnd",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(13)).getEntity()).getActivityId()));
     
     assertEquals(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, events.get(14).getType());
     assertEquals("mainEnd",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(14)).getEntity()).getActivityId()));
     assertNotNull("mainEnd",  ( ( (HistoricActivityInstance) ((ActivitiEntityEvent) events.get(14)).getEntity()).getEndTime()));
     
     // Process instance end
     assertEquals(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_ENDED, events.get(15).getType());
     
    }
  }

}
